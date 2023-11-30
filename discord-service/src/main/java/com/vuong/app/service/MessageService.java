package com.vuong.app.service;

import com.vuong.app.config.MysteryJdbc;
import com.vuong.app.doman.Message;
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class MessageService extends MessageServiceGrpc.MessageServiceImplBase {

    private final MysteryJdbc mysteryJdbc;
    private final MessagePublisher messagePublisher;

    @Override
    public void createMessage(GrpcCreateMessageRequest request, StreamObserver<GrpcCreateMessageResponse> responseObserver) {
        String getMemberQuery = "select " +
                "tbl_member.id as member_id, tbl_member.role as member_role, tbl_member.server_id as member_server_id, tbl_member.join_at as member_join_at, " +
                "tbl_profile.id as profile_id, tbl_profile.name as profile_name, tbl_profile.avt_url as profile_avt_url " +
                "from tbl_member inner join tbl_profile " +
                "on tbl_member.profile_id = tbl_profile.id " +
                "where tbl_member.profile_id = ? and tbl_member.server_id = (select tbl_channel.server_id from tbl_channel where tbl_channel.id = ?)";
        String insertMessageQuery = "insert into tbl_message(id, content, file_url, channel_id, created_by, created_at, updated_at) values(?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        ResultSet rs1 = null;

        try {
            con = mysteryJdbc.getConnection();

            pst1 = con.prepareStatement(getMemberQuery);
            pst1.setString(1, request.getProfileId());
            pst1.setString(2, request.getChannelId());

            rs1 = pst1.executeQuery();

            GrpcMessageEvent.GrpcMemberProfile memberProfile = null;

            while (rs1.next()) { // no permistion
                memberProfile = GrpcMessageEvent.GrpcMemberProfile.newBuilder()
                        .setMemberId(rs1.getString(1))
                        .setRole(GrpcMessageEvent.GrpcMemberRole.forNumber(rs1.getInt(2)))
                        .setServerId(rs1.getString(3))
                        .setJoinAt(rs1.getString(4))
                        .setProfileId(rs1.getString(5))
                        .setName(rs1.getString(6))
                        .setAvtUrl(rs1.getString(7))
                        .build();
            }

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
            String createdAt = Instant.now().toString();
            String updatedAt = createdAt;

            pst2 = con.prepareStatement(insertMessageQuery);
            pst2.setString(1, messageId);
            pst2.setString(2, request.hasContent() ? request.getContent() : null);
            pst2.setString(3, request.hasFileUrl() ? request.getFileUrl() : null);
            pst2.setString(4, request.getChannelId());
            pst2.setString(5, memberProfile.getProfileId());
            pst2.setString(6, createdAt);
            pst2.setString(7, updatedAt);

            int result = pst2.executeUpdate();

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
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs1);
            mysteryJdbc.closePreparedStatement(pst1, pst2);
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
        String messagesQuery = "select " +
                "            tbl_message.id, tbl_message.content, tbl_message.file_url, tbl_message.channel_id, tbl_message.created_at, tbl_message.updated_at, " +
                "            tbl_member.id, tbl_member.role, tbl_member.server_id, tbl_member.join_at, " +
                "            tbl_profile.id, tbl_profile.name, tbl_profile.avt_url " +
                "            from tbl_message " +
                "            inner join tbl_profile " +
                "            on tbl_message.created_by = tbl_profile.id " +
                "            inner join tbl_member " +
                "            on tbl_profile.id = tbl_member.profile_id " +
                "            where tbl_message.id = ? " +
                "            and tbl_message.deleted_at is null " +
                "            and tbl_profile.id = ?;";

        String updateMesageQuery = "update tbl_message set content = ?, updated_at = ? where tbl_message.id = ?";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        try {
            con = mysteryJdbc.getConnection();

            pst1 = con.prepareStatement(messagesQuery);
            pst1.setString(1, request.getMessageId());
            pst1.setString(2, request.getProfileId());

            rs1 = pst1.executeQuery();

            GrpcMessageEvent.GrpcMessage.Builder builder = GrpcMessageEvent.GrpcMessage.newBuilder();
            GrpcMessageEvent.GrpcMemberProfile.Builder memberProfileBuilder = GrpcMessageEvent.GrpcMemberProfile.newBuilder();
            boolean hasResult = false;

            while (rs1.next()) { // no permistion
                hasResult = true;
                builder.setMessageId(rs1.getString(1));
                builder.setContent(rs1.getString(2));
                if (rs1.getString(3) != null) {
                    builder.setFileUrl(rs1.getString(3));
                }
                builder.setChannelId(rs1.getString(4));
                builder.setCreatedAt(rs1.getString(5));
                builder.setUpdatedAt(rs1.getString(6));

                memberProfileBuilder.setMemberId(rs1.getString(7));
                memberProfileBuilder.setRole(GrpcMessageEvent.GrpcMemberRole.forNumber(rs1.getInt(8)));
                memberProfileBuilder.setServerId(rs1.getString(9));
                memberProfileBuilder.setJoinAt(rs1.getString(10));
                memberProfileBuilder.setProfileId(rs1.getString(11));
                memberProfileBuilder.setName(rs1.getString(12));
                memberProfileBuilder.setAvtUrl(rs1.getString(13));
            }

            GrpcMessageEvent.GrpcMemberProfile memberProfile = memberProfileBuilder.build();
            builder.setAuthor(memberProfile);

            if (!hasResult) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_NOT_FOUND;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not exists channel")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException(metadata));
                return;
            }

            String updatedAt = Instant.now().toString();

            pst2 = con.prepareStatement(updateMesageQuery);
            pst2.setString(1, request.getContent());
            pst2.setString(2, updatedAt);
            pst2.setString(3, request.getMessageId());

            int result = pst2.executeUpdate();

            GrpcMessageEvent.GrpcMessage grpcMessage = builder.build();

            GrpcMessageEvent grpcMessageEvent = GrpcMessageEvent.newBuilder()
                    .setType(GrpcMessageEvent.GrpcMessageEventType.MESSAGE_EVENT_TYPE_EDIT)
                    .setData(grpcMessage)
                    .build();

            GrpcEvent grpcEvent = GrpcEvent.newBuilder()
                    .setMessageEvent(grpcMessageEvent)
                    .build();

            this.messagePublisher.publish(memberProfile.getServerId(), grpcEvent);

            GrpcUpdateMessageResponse response = GrpcUpdateMessageResponse.newBuilder()
                    .setMessageId(request.getMessageId())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs1, rs2);
            mysteryJdbc.closePreparedStatement(pst1, pst2);
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
        String messagesQuery = "select " +
                "            tbl_message.id, tbl_message.channel_id, tbl_message.created_at, tbl_message.updated_at, " +
                "            tbl_channel.server_id, " +
                "            tbl_member.id, tbl_member.role, tbl_member.server_id, tbl_member.join_at, " +
                "            tbl_profile.id, tbl_profile.name, tbl_profile.avt_url " +
                "            from tbl_message " +
                "            inner join tbl_channel " +
                "            on tbl_message.channel_id = tbl_channel.id " +
                "            inner join tbl_profile " +
                "            on tbl_message.created_by = tbl_profile.id " +
                "            left join tbl_member " +
                "            on tbl_profile.id = tbl_member.profile_id " +
                "            where tbl_message.id = ? " +
                "            and tbl_message.deleted_at is null;";

        // else check member with profileId has permistion delete
        String getMemberQuery = "select " +
                "            tbl_member.id, tbl_member.role, tbl_member.server_id, tbl_member.join_at, " +
                "            tbl_profile.id, tbl_profile.name, tbl_profile.avt_url " +
                "            from tbl_member " +
                "            inner join tbl_profile " +
                "            on tbl_member.profile_id = tbl_profile.id " +
                "            where tbl_profile.id = ? " +
                "            and tbl_member.server_id = ? " +
                "            and tbl_member.role in (?, ?);";
        String updateMessageQuery = "update tbl_message set deleted_at = ?, deleted_by = ? where tbl_message.id = ?";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        PreparedStatement pst3 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        try {
            con = mysteryJdbc.getConnection();

            pst1 = con.prepareStatement(messagesQuery);
            pst1.setString(1, request.getMessageId());

            rs1 = pst1.executeQuery();

            if (!mysteryJdbc.hasResult(rs1)) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_NOT_FOUND;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not found")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException(metadata));
                return;
            }

            GrpcMessageEvent.GrpcMessage.Builder builder = GrpcMessageEvent.GrpcMessage.newBuilder();
            GrpcMessageEvent.GrpcMemberProfile.Builder memberProfileBuilder = GrpcMessageEvent.GrpcMemberProfile.newBuilder();
            String serverId = null;
            String memberOwnerMessageId = null;

            while (rs1.next()) { // no permistion
                builder.setMessageId(rs1.getString(1));
                builder.setChannelId(rs1.getString(2));
                builder.setCreatedAt(rs1.getString(3));
                builder.setUpdatedAt(rs1.getString(4));
                serverId = rs2.getString(5);
                memberOwnerMessageId = rs1.getString(6);

                if (rs1.getString(7) != null) {
                    memberProfileBuilder.setRole(GrpcMessageEvent.GrpcMemberRole.forNumber(rs1.getInt(7)));
                }
                if (rs1.getString(8) != null) {
                    memberProfileBuilder.setServerId(rs1.getString(8));
                }
                if (rs1.getString(9) != null) {
                    memberProfileBuilder.setJoinAt(rs1.getString(9));
                }
                memberProfileBuilder.setProfileId(rs1.getString(10));
                memberProfileBuilder.setName(rs1.getString(11));
                if (rs1.getString(12) != null) {
                    memberProfileBuilder.setAvtUrl(rs1.getString(12));
                }
            }

            GrpcMessageEvent.GrpcMemberProfile memberProfile = memberProfileBuilder.build();

            boolean isOwner = memberOwnerMessageId != null && memberProfile.getProfileId().equals(request.getProfileId());
            boolean isPermission = false;

            if (!isOwner) {
                pst2 = con.prepareStatement(getMemberQuery);
                pst2.setString(1, request.getProfileId());
                pst2.setString(2, serverId);
                pst2.setInt(3, GrpcMemberRole.MEMBER_ROLE_ADMIN_VALUE);
                pst2.setInt(4, GrpcMemberRole.MEMBER_ROLE_MODERATOR_VALUE);

                rs2 = pst2.executeQuery();

                isPermission = mysteryJdbc.hasResult(rs2);
            }

            boolean canDelete = isOwner || isPermission;

            if (!canDelete) {
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

            String deletedAt = Instant.now().toString();

            pst3 = con.prepareStatement(updateMessageQuery);
            pst3.setString(1, deletedAt);
            pst3.setString(2, memberProfile.getProfileId());
            pst3.setString(3, request.getMessageId());

            int result = pst3.executeUpdate();

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

            this.messagePublisher.publish(memberProfile.getServerId(), grpcEvent);

            GrpcDeleteMessageResponse response = GrpcDeleteMessageResponse.newBuilder()
                    .setDeleted(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs1, rs2);
            mysteryJdbc.closePreparedStatement(pst1, pst2, pst3);
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
        String memberQuery = "select tbl_member.server_id " +
                "from tbl_member " +
                "where tbl_member.profile_id = ? and tbl_member.server_id = (select tbl_channel.server_id from tbl_channel where tbl_channel.id = ?)";

        String countQuery = "select count(tbl_message.id) from tbl_message where tbl_message.channel_id = ?";

        String messagesQuery = "select " +
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
                "            and tbl_member.server_id = ? " +
                "            order by tbl_message.created_at desc " +
                "            limit ? offset ?;";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        PreparedStatement pst3 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        ResultSet rs3 = null;

        GrpcGetMessagesByChannelIdResponse.Builder builder = GrpcGetMessagesByChannelIdResponse.newBuilder();

        try {
            con = mysteryJdbc.getConnection();

            pst1 = con.prepareStatement(memberQuery);
            pst1.setString(1, request.getProfileId());
            pst1.setString(2, request.getChannelId());

            rs1 = pst1.executeQuery();

            if (!mysteryJdbc.hasResult(rs1)) { // no permistion
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

            String serverId = null;

            while (rs1.next()) {
                serverId = rs1.getString(1);
            }

            pst2 = con.prepareStatement(countQuery);

            pst2.setString(1, request.getChannelId());

            rs2 = pst2.executeQuery();

            long totalElements = 0;

            while (rs2.next()) {
                totalElements = rs2.getLong(1);
            }

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

            pst3 = con.prepareStatement(messagesQuery);
            pst3.setString(1, request.getChannelId());
            pst3.setString(2, serverId);
            pst3.setInt(3, request.getPageSize());
            pst3.setInt(4, request.getPageNumber() * request.getPageSize());

            rs3 = pst3.executeQuery();

            while (rs3.next()) {
                GrpcMessage.Builder builderMessage = GrpcMessage.newBuilder();
                builderMessage.setMessageId(rs3.getString(1));
                if (rs3.getString(2) != null) {
                    builderMessage.setContent(rs3.getString(2));
                }
                if (rs3.getString(3) != null) {
                    builderMessage.setFileUrl(rs3.getString(3));
                }
                builderMessage.setChannelId(rs3.getString(4));
                builderMessage.setCreatedAt(rs3.getString(5));
                builderMessage.setUpdatedAt(rs3.getString(6));
                if (rs3.getString(7) != null) {
                    builderMessage.setDeletedAt(rs3.getString(7));
                }
                if (rs3.getString(8) != null) {
                    builderMessage.setDeletedBy(rs3.getString(8));
                }

                GrpcMemberProfile.Builder authorBuilder = GrpcMemberProfile.newBuilder();

                authorBuilder.setMemberId(rs3.getString(9));
                authorBuilder.setRole(GrpcMemberRole.forNumber(rs3.getInt(10)));
                authorBuilder.setServerId(rs3.getString(11));
                authorBuilder.setJoinAt(rs3.getString(12));
                authorBuilder.setProfileId(rs3.getString(13));
                authorBuilder.setName(rs3.getString(14));
                if (rs3.getString(15) != null) {
                    authorBuilder.setAvtUrl(rs3.getString(15));
                }
                builderMessage.setAuthor(authorBuilder.build());

                GrpcMessage message = builderMessage.build();

                builder.addContent(message);
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            mysteryJdbc.closeResultSet(rs1, rs2, rs3);
            mysteryJdbc.closePreparedStatement(pst1, pst2, pst3);
        }
    }
}
