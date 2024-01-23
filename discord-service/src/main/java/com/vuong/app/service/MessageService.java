package com.vuong.app.service;

import com.vuong.app.config.JdbcClient;
import com.vuong.app.config.JdbcTemplate;
import com.vuong.app.config.JdbcUtils;
import com.vuong.app.redis.MessagePublisher;
import com.vuong.app.v1.GrpcErrorCode;
import com.vuong.app.v1.GrpcErrorResponse;
import com.vuong.app.v1.GrpcMeta;
import com.vuong.app.v1.discord.*;
import com.vuong.app.v1.event.GrpcEvent;
import com.vuong.app.v1.event.GrpcMessageEvent;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class MessageService extends MessageServiceGrpc.MessageServiceImplBase {

    private final DataSource dataSource;
    private final MessagePublisher messagePublisher;

    @Override
    public void createMessage(GrpcCreateMessageRequest request, StreamObserver<GrpcCreateMessageResponse> responseObserver) {
        String GET_MEMBER_QUERY = "select " +
                "tbl_member.id as member_id, tbl_member.role as member_role, tbl_member.server_id as member_server_id, tbl_member.join_at as member_join_at, " +
                "tbl_profile.id as profile_id, tbl_profile.name as profile_name, tbl_profile.avt_url as profile_avt_url " +
                "from tbl_member inner join tbl_profile " +
                "on tbl_member.profile_id = tbl_profile.id " +
                "where tbl_member.profile_id = ? and tbl_member.server_id = (select tbl_channel.server_id from tbl_channel where tbl_channel.id = ?)";
        String INSERT_MESSAGE_QUERY = "insert into tbl_message(id, content, file_url, channel_id, created_by, created_at, updated_at) values(?, ?, ?, ?, ?, ?, ?)";

        try {
            JdbcUtils.openSession(dataSource);

            GrpcMessageEvent.GrpcMemberProfile memberProfile = JdbcTemplate.query(
                    GET_MEMBER_QUERY,
                    List.of(
                            request.getProfileId(),
                            request.getChannelId()
                    ),
                    rs -> {
                        while (rs.next()) { // no permistion
                            return GrpcMessageEvent.GrpcMemberProfile.newBuilder()
                                    .setMemberId(rs.getString(1))
                                    .setRole(GrpcMessageEvent.GrpcMemberRole.forNumber(rs.getInt(2)))
                                    .setServerId(rs.getString(3))
                                    .setJoinAt(rs.getString(4))
                                    .setProfileId(rs.getString(5))
                                    .setName(rs.getString(6))
                                    .setAvtUrl(rs.getString(7))
                                    .build();
                        }

                        return null;
                    }
            );

            if (memberProfile == null) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_PERMISSION_DENIED;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not permistion")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException(metadata));
                return;
            }

            String messageId = UUID.randomUUID().toString();
            Instant now = Instant.now();
            String createdAt = now.toString();
            String updatedAt = now.toString();

            JdbcTemplate.create(
                    INSERT_MESSAGE_QUERY,
                    List.of(
                            messageId,
                            request.hasContent() ? request.getContent() : null,
                            request.hasFileUrl() ? request.getFileUrl() : null,
                            request.getChannelId(),
                            memberProfile.getProfileId(),
                            createdAt,
                            updatedAt
                    )
            );

            GrpcMessageEvent.GrpcMessage.Builder builder = GrpcMessageEvent.GrpcMessage.newBuilder();
            builder.setMessageId(messageId);
            if (request.hasContent()) {
                builder.setContent(request.getContent());
            }
            if (request.hasFileUrl()) {
                builder.setFileUrl(request.getFileUrl());
            }
            builder.setChannelId(request.getChannelId());
            builder.setCreatedAt(createdAt);
            builder.setUpdatedAt(updatedAt);
            builder.setAuthor(memberProfile);

            GrpcMessageEvent.GrpcMessage grpcMessage = builder.build();

            GrpcMessageEvent grpcMessageEvent = GrpcMessageEvent.newBuilder()
                    .setType(GrpcMessageEvent.GrpcMessageEventType.MESSAGE_EVENT_TYPE_ADD)
                    .setData(grpcMessage)
                    .build();

            GrpcEvent grpcEvent = GrpcEvent.newBuilder()
                    .setMessageEvent(grpcMessageEvent)
                    .build();

            this.messagePublisher.publish(memberProfile.getServerId(), grpcEvent);

            GrpcCreateMessageResponse response = GrpcCreateMessageResponse.newBuilder()
                    .setMessageId(messageId)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            JdbcUtils.doRollback();
        } finally {
            JdbcUtils.closeConnection();
        }
    }

    /*
        Check has message => has channel => has server, owner message by profileId
        select
            tbl_message.id, tbl_message.content, tbl_message.file_url, tbl_message.channel_id, tbl_message.created_at, tbl_message.updated_at,
            tbl_member.id, tbl_member.role, tbl_member.server_id, tbl_member.join_at,
            tbl_profile.id, tbl_profile.name, tbl_profile.avt_url
            from tbl_message
            inner join tbl_profile
            on tbl_message.created_by = tbl_profile.id
            inner join tbl_member
            on tbl_profile.id = tbl_member.profile_id
            where tbl_message.id = '3cc49d9f-5af5-4e6f-ac92-869a3759e2ce'
            and tbl_message.deleted_at is null
            and tbl_profile.id = '9fad9a7d-1a1b-47f2-9cea-66abb7719968'
            and tbl_member.server_id = 'b8ae3f8e-3931-49f8-8982-df057c68eeab';
    */

    @Override
    public void updateMessage(GrpcUpdateMessageRequest request, StreamObserver<GrpcUpdateMessageResponse> responseObserver) {
        // owner message
        String MESSAGES_QUERY = "select " +
                "            tbl_message.id, tbl_message.channel_id, tbl_message.created_at, tbl_message.updated_at, " +
                "            tbl_channel.server_id, " +
                "            tbl_profile.id, tbl_profile.name, tbl_profile.avt_url " +
                "            from tbl_message " +
                "            inner join tbl_channel " +
                "            on tbl_message.channel_id = tbl_channel.id " +
                "            inner join tbl_profile " +
                "            on tbl_message.created_by = tbl_profile.id " +
                "            where tbl_message.id = ? " +
                "            and tbl_message.deleted_at is null";

        String MEMBER_OWNER_MESSAGE_QUERY = "select " +
                "            tbl_member.id, tbl_member.role, tbl_member.server_id, tbl_member.join_at " +
                "            from tbl_member " +
                "            where tbl_member.profile_id = ? " +
                "            and tbl_member.server_id = ?";

        String UPDATE_MESAGE_QUERY = "update tbl_message set content = ?, updated_at = ? where tbl_message.id = ?";

        try {
            JdbcUtils.openSession(dataSource);

            GrpcMessageEvent.GrpcMessage.Builder builder = GrpcMessageEvent.GrpcMessage.newBuilder();
            GrpcMessageEvent.GrpcMemberProfile.Builder memberProfileBuilder = GrpcMessageEvent.GrpcMemberProfile.newBuilder();

            List<String> result = JdbcClient.sql(MESSAGES_QUERY)
                    .params(request.getMessageId())
                    .query(rs -> {
                        while (rs.next()) {
                            builder.setMessageId(rs.getString(1));
                            builder.setChannelId(rs.getString(2));
                            builder.setCreatedAt(rs.getString(3));
                            builder.setUpdatedAt(rs.getString(4));
                            String serverId = rs.getString(5);
                            String profileIdOwnerMessage = rs.getString(6);
                            memberProfileBuilder.setProfileId(profileIdOwnerMessage);
                            memberProfileBuilder.setName(rs.getString(7));
                            if (rs.getString(8) != null) {
                                memberProfileBuilder.setAvtUrl(rs.getString(8));
                            }
                            return List.of(serverId, profileIdOwnerMessage);
                        }
                        return null;
                    });

            if (result == null) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_NOT_FOUND;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not has message by messageId")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException(metadata));
                return;
            }

            String serverId = result.get(0);
            String profileIdOwnerMessage = result.get(1);

            boolean isOwnerAction = profileIdOwnerMessage.equals(request.getProfileId());

            String updatedAt = Instant.now().toString();

            if (!isOwnerAction) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_PERMISSION_DENIED;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not permission")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.PERMISSION_DENIED.asRuntimeException(metadata));
                return;
            }

            boolean isMember = JdbcClient.sql(MEMBER_OWNER_MESSAGE_QUERY)
                    .params(
                            profileIdOwnerMessage,
                            serverId
                    )
                    .query(rs -> {
                        while (rs.next()) {
                            memberProfileBuilder.setMemberId(rs.getString(1));
                            memberProfileBuilder.setRole(GrpcMessageEvent.GrpcMemberRole.forNumber(rs.getInt(2)));
                            memberProfileBuilder.setServerId(rs.getString(3));
                            memberProfileBuilder.setJoinAt(rs.getString(4));
                            return true;
                        }
                        return false;
                    });

            if (!isMember) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_PERMISSION_DENIED;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not permission")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.PERMISSION_DENIED.asRuntimeException(metadata));
                return;
            }

            JdbcClient.sql(UPDATE_MESAGE_QUERY)
                    .params(
                            request.getContent(),
                            updatedAt,
                            request.getMessageId()
                    )
                    .update();

            JdbcUtils.doCommit();

            GrpcMessageEvent.GrpcMemberProfile memberProfile = memberProfileBuilder.build();

            builder.setContent(request.getContent());
            builder.setAuthor(memberProfile);
            builder.setUpdatedAt(updatedAt);
            builder.setDeletedBy(request.getProfileId());

            GrpcMessageEvent.GrpcMessage grpcMessage = builder.build();

            GrpcMessageEvent grpcMessageEvent = GrpcMessageEvent.newBuilder()
                    .setType(GrpcMessageEvent.GrpcMessageEventType.MESSAGE_EVENT_TYPE_EDIT)
                    .setData(grpcMessage)
                    .build();

            GrpcEvent grpcEvent = GrpcEvent.newBuilder()
                    .setMessageEvent(grpcMessageEvent)
                    .build();

            this.messagePublisher.publish(serverId, grpcEvent);

            GrpcUpdateMessageResponse response = GrpcUpdateMessageResponse.newBuilder()
                    .setMessageId(request.getMessageId())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (SQLException ex) {
            JdbcUtils.doRollback();
        } finally {
            JdbcUtils.closeConnection();
        }
    }

    /*
        get memberProfile => isOnwer or Member admin or moderator
        select
            tbl_member.id, tbl_member.role, tbl_member.server_id, tbl_member.join_at,
            tbl_profile.id, tbl_profile.name, tbl_profile.avt_url
            from tbl_member
            inner join tbl_profile
            on tbl_profile.id = tbl_member.profile_id
            where tbl_profile.id = '9fad9a7d-1a1b-47f2-9cea-66abb7719968'
            and tbl_member.server_id = 'b8ae3f8e-3931-49f8-8982-df057c68eeab';

        select
            tbl_message.id, tbl_message.content, tbl_message.file_url, tbl_message.channel_id, tbl_message.created_at, tbl_message.updated_at
            from tbl_message
            where tbl_message.id = '3cc49d9f-5af5-4e6f-ac92-869a3759e2ce'
            and tbl_message.deleted_at is null;
     */

    @Override
    public void deleteMessage(GrpcDeleteMessageRequest request, StreamObserver<GrpcDeleteMessageResponse> responseObserver) {
//        String getMessageQuery = "select " +
//                "            tbl_message.id, tbl_message.content, tbl_message.file_url, tbl_message.channel_id, tbl_message.created_by, tbl_message.created_at, tbl_message.updated_at " +
//                "            from tbl_message " +
//                "            where tbl_message.id = ? " +
//                "            and tbl_message.deleted_at is null;";
        // if has tbl_member.profile_id == profileId => owner message delete;
        String MESSAGES_QUERY = "select " +
                "            tbl_message.id, tbl_message.channel_id, tbl_message.created_at, tbl_message.updated_at, " +
                "            tbl_channel.server_id, " +
                "            tbl_profile.id, tbl_profile.name, tbl_profile.avt_url " +
                "            from tbl_message " +
                "            inner join tbl_channel " +
                "            on tbl_message.channel_id = tbl_channel.id " +
                "            inner join tbl_profile " +
                "            on tbl_message.created_by = tbl_profile.id " +
                "            where tbl_message.id = ? " +
                "            and tbl_message.deleted_at is null";

        String MEMBER_OWNER_MESSAGE_QUERY = "select " +
                "            tbl_member.id, tbl_member.role, tbl_member.server_id, tbl_member.join_at " +
                "            from tbl_member " +
                "            where tbl_member.profile_id = ? " +
                "            and tbl_member.server_id = ?";

        String MEMBER_PERMISSION_QUERY = "select " +
                "            tbl_member.id, tbl_member.role, tbl_member.server_id, tbl_member.join_at, " +
                "            tbl_profile.id, tbl_profile.name, tbl_profile.avt_url " +
                "            from tbl_member " +
                "            inner join tbl_profile " +
                "            on tbl_member.profile_id = tbl_profile.id " +
                "            where tbl_member.profile_id = ? " +
                "            and tbl_member.server_id = ? " +
                "            and tbl_member.role in (?, ?);";

//        String messagesQuery = "select " +
//                "            tbl_message.id, tbl_message.channel_id, tbl_message.created_at, tbl_message.updated_at, " +
//                "            tbl_channel.server_id, " +
//                "            tbl_member.id, tbl_member.role, tbl_member.server_id, tbl_member.join_at, " +
//                "            tbl_profile.id, tbl_profile.name, tbl_profile.avt_url " +
//                "            from tbl_message " +
//                "            inner join tbl_channel " +
//                "            on tbl_message.channel_id = tbl_channel.id " +
//                "            inner join tbl_profile " +
//                "            on tbl_message.created_by = tbl_profile.id " +
//                "            left join tbl_member " +
//                "            on tbl_message.created_by = tbl_member.profile_id " +
//                "            where tbl_message.id = ? " +
//                "            and tbl_message.deleted_at is null;";

        // else check member with profileId has permistion delete
//        String getMemberQuery = "select " +
//                "            tbl_member.id, tbl_member.role, tbl_member.server_id, tbl_member.join_at, " +
//                "            tbl_profile.id, tbl_profile.name, tbl_profile.avt_url " +
//                "            from tbl_member " +
//                "            inner join tbl_profile " +
//                "            on tbl_member.profile_id = tbl_profile.id " +
//                "            where tbl_profile.id = ? " +
//                "            and tbl_member.server_id = ? " +
//                "            and tbl_member.role in (?, ?);";
        String UPDATE_MESSAGE_QUERY = "update tbl_message set deleted_at = ?, deleted_by = ? where tbl_message.id = ?";

        try {

            JdbcUtils.openSession(dataSource);

            GrpcMessageEvent.GrpcMessage.Builder builder = GrpcMessageEvent.GrpcMessage.newBuilder();
            GrpcMessageEvent.GrpcMemberProfile.Builder memberProfileBuilder = GrpcMessageEvent.GrpcMemberProfile.newBuilder();
            List<String> result = JdbcClient.sql(MESSAGES_QUERY)
                    .params(request.getMessageId())
                    .query(rs -> {
                        while (rs.next()) {
                            builder.setMessageId(rs.getString(1));
                            builder.setChannelId(rs.getString(2));
                            builder.setCreatedAt(rs.getString(3));
                            builder.setUpdatedAt(rs.getString(4));
                            String serverId = rs.getString(5);
                            String profileIdOwnerMessage = rs.getString(6);
                            memberProfileBuilder.setProfileId(profileIdOwnerMessage);
                            memberProfileBuilder.setName(rs.getString(7));
                            if (rs.getString(8) != null) {
                                memberProfileBuilder.setAvtUrl(rs.getString(8));
                            }
                            return List.of(serverId, profileIdOwnerMessage);
                        }
                        return null;
                    });

            if (result == null) {
                GrpcDeleteMessageResponse response = GrpcDeleteMessageResponse.newBuilder()
                        .setDeleted(true)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            String serverId = result.get(0);
            String profileIdOwnerMessage = result.get(1);

            boolean isOwnerAction = profileIdOwnerMessage.equals(request.getProfileId());

            String deletedAt = Instant.now().toString();

            if (isOwnerAction) {
                boolean isMember = JdbcClient.sql(MEMBER_OWNER_MESSAGE_QUERY)
                        .params(
                                profileIdOwnerMessage,
                                serverId
                        )
                        .query(rs -> {
                            while (rs.next()) {
                                memberProfileBuilder.setMemberId(rs.getString(1));
                                memberProfileBuilder.setRole(GrpcMessageEvent.GrpcMemberRole.forNumber(rs.getInt(2)));
                                memberProfileBuilder.setServerId(rs.getString(3));
                                memberProfileBuilder.setJoinAt(rs.getString(4));
                                return true;
                            }
                            return false;
                        });

                if (!isMember) {
                    Metadata metadata = new Metadata();
                    Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                    GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_PERMISSION_DENIED;
                    GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                            .setErrorCode(errorCode)
                            .setMessage("not permission")
                            .build();
                    // pass the error object via metadata
                    metadata.put(responseKey, errorResponse);
                    responseObserver.onError(Status.PERMISSION_DENIED.asRuntimeException(metadata));
                    return;
                }

                JdbcClient.sql(UPDATE_MESSAGE_QUERY)
                        .params(
                                deletedAt,
                                request.getProfileId(),
                                request.getMessageId()
                        )
                        .update();

                JdbcUtils.doCommit();

                GrpcMessageEvent.GrpcMemberProfile memberProfile = memberProfileBuilder.build();

                builder.setContent("This message has been deleted.");
                builder.setAuthor(memberProfile);
                builder.setDeletedAt(deletedAt);
                builder.setDeletedBy(request.getProfileId());

                GrpcMessageEvent.GrpcMessage grpcMessage = builder.build();

                GrpcMessageEvent grpcMessageEvent = GrpcMessageEvent.newBuilder()
                        .setType(GrpcMessageEvent.GrpcMessageEventType.MESSAGE_EVENT_TYPE_EDIT)
                        .setData(grpcMessage)
                        .build();

                GrpcEvent grpcEvent = GrpcEvent.newBuilder()
                        .setMessageEvent(grpcMessageEvent)
                        .build();

                this.messagePublisher.publish(serverId, grpcEvent);

                GrpcDeleteMessageResponse response = GrpcDeleteMessageResponse.newBuilder()
                        .setDeleted(true)
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();

                return;
            }

            boolean isPermission = JdbcClient.sql(MEMBER_PERMISSION_QUERY)
                    .params(
                            request.getProfileId(),
                            serverId,
                            GrpcMemberRole.MEMBER_ROLE_ADMIN_VALUE,
                            GrpcMemberRole.MEMBER_ROLE_MODERATOR_VALUE
                    )
                    .exists();

            if (!isPermission) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_PERMISSION_DENIED;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not permission")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.PERMISSION_DENIED.asRuntimeException(metadata));
                return;
            }

            JdbcClient.sql(UPDATE_MESSAGE_QUERY)
                    .params(
                            deletedAt,
                            request.getProfileId(),
                            request.getMessageId()
                    )
                    .update();

            JdbcUtils.doCommit();

            GrpcMessageEvent.GrpcMemberProfile memberProfile = memberProfileBuilder.build();

            builder.setContent("This message has been deleted.");
            builder.setAuthor(memberProfile);
            builder.setDeletedAt(deletedAt);
            builder.setDeletedBy(request.getProfileId());

            GrpcMessageEvent.GrpcMessage grpcMessage = builder.build();

            GrpcMessageEvent grpcMessageEvent = GrpcMessageEvent.newBuilder()
                    .setType(GrpcMessageEvent.GrpcMessageEventType.MESSAGE_EVENT_TYPE_EDIT)
                    .setData(grpcMessage)
                    .build();

            GrpcEvent grpcEvent = GrpcEvent.newBuilder()
                    .setMessageEvent(grpcMessageEvent)
                    .build();

            this.messagePublisher.publish(serverId, grpcEvent);

            GrpcDeleteMessageResponse response = GrpcDeleteMessageResponse.newBuilder()
                    .setDeleted(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JdbcUtils.doRollback();
        } finally {
            JdbcUtils.closeConnection();
        }
    }

    /*
        select
            tbl_message.id,
            case
                when tbl_message.deleted_at is null then tbl_message.content
                else 'This message has been deleted.'
            end as content,
            case
                when tbl_message.deleted_at is null then tbl_message.file_url
                else null
            end as file_url, tbl_message.channel_id, tbl_message.created_at, tbl_message.updated_at, tbl_message.deleted_at, tbl_message.deleted_by,
            tbl_member.id, tbl_member.role, tbl_member.server_id, tbl_member.join_at,
            tbl_profile.id, tbl_profile.name, tbl_profile.avt_url
            from tbl_message
            inner join tbl_profile
            on tbl_message.created_by = tbl_profile.id
            left join tbl_member
            on tbl_profile.id = tbl_member.profile_id
            where tbl_message.channel_id = '3cc49d9f-5af5-4e6f-ac92-869a3759e2ce'
            order by tbl_message.created_at desc
            limit 10 offset 1;
     */

    @Override
    public void getMessagesByChannelId(GrpcGetMessagesByChannelIdRequest request, StreamObserver<GrpcGetMessagesByChannelIdResponse> responseObserver) {
        String MEMBER_QUERY = "select tbl_member.server_id " +
                "from tbl_member " +
                "where tbl_member.profile_id = ? and tbl_member.server_id = (select tbl_channel.server_id from tbl_channel where tbl_channel.id = ?)";

        String COUNT_QUERY = "select count(tbl_message.id) from tbl_message where tbl_message.channel_id = ?";

        String MESSAGES_QUERY = "select " +
                "            tbl_message.id, " +
                "            case " +
                "                when tbl_message.deleted_at is null then tbl_message.content " +
                "                else 'This message has been deleted.' " +
                "            end as content, " +
                "            case " +
                "                when tbl_message.deleted_at is null then tbl_message.file_url " +
                "                else null " +
                "            end as file_url, tbl_message.channel_id, tbl_message.created_at, tbl_message.updated_at, tbl_message.deleted_at, tbl_message.deleted_by, " +
                "            tbl_member.id, tbl_member.role, tbl_member.server_id, tbl_member.join_at, " +
                "            tbl_profile.id, tbl_profile.name, tbl_profile.avt_url " +
                "            from tbl_message " +
                "            inner join tbl_profile " +
                "            on tbl_message.created_by = tbl_profile.id " +
                "            left join tbl_member " +
                "            on tbl_message.created_by = tbl_member.profile_id " +
                "            where tbl_message.channel_id = ? " +
                "            and tbl_member.server_id is null or tbl_member.server_id = ? " +
                "            order by tbl_message.created_at desc " +
                "            limit ? offset ?;";

        GrpcGetMessagesByChannelIdResponse.Builder builder = GrpcGetMessagesByChannelIdResponse.newBuilder();

        try {

            JdbcUtils.initConnection(dataSource);

            String serverId = JdbcClient.sql(MEMBER_QUERY)
                    .params(
                            request.getProfileId(),
                            request.getChannelId()
                    )
                    .query(rs -> {
                        while (rs.next()) {
                            return rs.getString(1);
                        }
                        return null;
                    });

            if (serverId == null) { // no permistion
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_NOT_FOUND;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not permistion")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException(metadata));
                return;
            }

            long totalElements = JdbcClient.sql(COUNT_QUERY)
                    .params(request.getChannelId())
                    .count();

            if (totalElements == 0) {
                GrpcMeta meta = GrpcMeta.newBuilder()
                        .setTotalElements(0)
                        .setTotalPages(0)
                        .setPageNumber(request.getPageNumber())
                        .setPageSize(request.getPageSize())
                        .build();
                builder.setMeta(meta);

                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
                return;
            }

            GrpcMeta meta = GrpcMeta.newBuilder()
                    .setTotalElements(totalElements)
                    .setTotalPages(totalElements == 0 ? 1 : (int)Math.ceil((double)totalElements / (double)request.getPageSize()))
                    .setPageNumber(request.getPageNumber())
                    .setPageSize(request.getPageSize())
                    .build();

            builder.setMeta(meta);

            JdbcClient.sql(MESSAGES_QUERY)
                    .params(
                            request.getChannelId(),
                            serverId,
                            request.getPageSize(),
                            request.getPageNumber() * request.getPageSize()
                    )
                    .query(rs -> {
                        while (rs.next()) {
                            GrpcMessage.Builder builderMessage = GrpcMessage.newBuilder();
                            builderMessage.setMessageId(rs.getString(1));
                            if (rs.getString(2) != null) {
                                builderMessage.setContent(rs.getString(2));
                            }
                            if (rs.getString(3) != null) {
                                builderMessage.setFileUrl(rs.getString(3));
                            }
                            builderMessage.setChannelId(rs.getString(4));
                            builderMessage.setCreatedAt(rs.getString(5));
                            builderMessage.setUpdatedAt(rs.getString(6));
                            if (rs.getString(7) != null) {
                                builderMessage.setDeletedAt(rs.getString(7));
                            }
                            if (rs.getString(8) != null) {
                                builderMessage.setDeletedBy(rs.getString(8));
                            }

                            GrpcMemberProfile.Builder authorBuilder = GrpcMemberProfile.newBuilder();

                            if (rs.getString(9) != null) {
                                authorBuilder.setMemberId(rs.getString(9));
                                authorBuilder.setRole(GrpcMemberRole.forNumber(rs.getInt(10)));
                                authorBuilder.setServerId(rs.getString(11));
                                authorBuilder.setJoinAt(rs.getString(12));
                            }
                            authorBuilder.setProfileId(rs.getString(13));
                            authorBuilder.setName(rs.getString(14));
                            if (rs.getString(15) != null) {
                                authorBuilder.setAvtUrl(rs.getString(15));
                            }
                            builderMessage.setAuthor(authorBuilder.build());

                            GrpcMessage message = builderMessage.build();

                            builder.addContent(message);
                        }
                        return null;
                    });

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            JdbcUtils.closeConnection();
        }
    }
}
