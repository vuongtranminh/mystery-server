package com.vuong.app.service;

import com.vuong.app.common.ServiceHelper;
import com.vuong.app.config.MysteryJdbc;
import com.vuong.app.redis.MessagePublisher;
import com.vuong.app.v1.GrpcErrorCode;
import com.vuong.app.v1.GrpcMeta;
import com.vuong.app.v1.GrpcRequest;
import com.vuong.app.v1.GrpcResponse;
import com.vuong.app.v1.discord.*;
import com.vuong.app.v1.user.GrpcCreateUserRequest;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class MessageService extends MessageServiceGrpc.MessageServiceImplBase {

    private final MysteryJdbc mysteryJdbc;
    private final MessagePublisher messagePublisher;

    @Override
    public void createMessage(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcCreateMessageRequest req = ServiceHelper.unpackedRequest(request, GrpcCreateMessageRequest.class);

        String getMemberQuery = "select " +
                "tbl_member.id as member_id, tbl_member.role as member_role, tbl_member.server_id as member_server_id, tbl_member.join_at as member_join_at, " +
                "tbl_profile.id as profile_id, tbl_profile.name as profile_name, tbl_profile.avt_url as profile_avt_url " +
                "from tbl_member inner join tbl_profile on tbl_member.profile_id = tbl_profile.id where tbl_member.profile_id = ? and tbl_member.server_id = ?";
        String existsChannelQuery = "select 1 from tbl_channel as c where c.id = ? and c.server_id = ?";
        String insertMessageQuery = "insert into tbl_message(id, content, file_url, channel_id, created_by, created_at, updated_at) values(?, ?, ?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        PreparedStatement pst3 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        try {
            con = mysteryJdbc.getConnection();

            pst1 = con.prepareStatement(getMemberQuery);
            pst1.setString(1, req.getProfileId());
            pst1.setString(2, req.getServerId());

            rs1 = pst1.executeQuery();

            GrpcMemberProfile memberProfile = null;

            while (rs1.next()) { // no permistion
                memberProfile = GrpcMemberProfile.newBuilder()
                        .setMemberId(rs1.getString(1))
                        .setRole(GrpcMemberRole.forNumber(rs1.getInt(2)))
                        .setServerId(rs1.getString(3))
                        .setJoinAt(rs1.getString(4))
                        .setProfileId(rs1.getString(5))
                        .setName(rs1.getString(6))
                        .setAvtUrl(rs1.getString(7))
                        .build();
            }

            if (memberProfile == null) {
                ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "user not found with id"));
                return;
            }

            pst2 = con.prepareStatement(existsChannelQuery);
            pst2.setString(1, req.getChannelId());
            pst2.setString(2, req.getServerId());

            rs2 = pst2.executeQuery();

            boolean existsChannel = rs2.next();

            if (!existsChannel) { // no permistion
                ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "user not found with id"));
                return;
            }

            String messageId = UUID.randomUUID().toString();
            String createdAt = Instant.now().toString();
            String updatedAt = createdAt;

            pst3 = con.prepareStatement(insertMessageQuery);
            pst3.setString(1, messageId);
            pst3.setString(2, req.hasContent() ? req.getContent() : null);
            pst3.setString(3, req.hasFileUrl() ? req.getFileUrl() : null);
            pst3.setString(4, req.getChannelId());
            pst3.setString(5, memberProfile.getProfileId());
            pst3.setString(6, createdAt);
            pst3.setString(7, updatedAt);

            int result = pst3.executeUpdate();

            GrpcMessage.Builder builder = GrpcMessage.newBuilder();
            builder.setMessageId(messageId);
            if (req.hasContent()) {
                builder.setContent(req.getContent());
            }
            if (req.hasFileUrl()) {
                builder.setFileUrl(req.getFileUrl());
            }
            builder.setChannelId(req.getChannelId());
            builder.setCreatedAt(createdAt);
            builder.setUpdatedAt(updatedAt);
            builder.setAuthor(memberProfile);

            GrpcMessage message = builder.build();

            GrpcMessageEvent messageEvent = GrpcMessageEvent.newBuilder()
                    .setType(GrpcMessageEventType.MESSAGE_EVENT_TYPE_ADD)
                    .setMessage(message)
                    .build();

            this.messagePublisher.publish(req.getServerId(), messageEvent);

            GrpcCreateMessageResponse response = GrpcCreateMessageResponse.newBuilder()
                    .setMessageId(messageId)
                    .build();

            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(response));
        } catch (SQLException ex) {
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs1, rs2);
            mysteryJdbc.closePreparedStatement(pst1, pst2, pst3);
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
    public void updateMessage(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
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
                "            and tbl_profile.id = ? " +
                "            and tbl_member.server_id = ?;";

        String updateMessageQuery = "update tbl_message set content = ?, updated_at = ? where tbl_message.id = ?";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        int result = 0;

        try {
            con = getConnection();

            pst1 = con.prepareStatement(messagesQuery);
            pst1.setString(1, update.getMessageId());
            pst1.setString(2, update.getProfileId());
            pst1.setString(3, update.getServerId());

            rs1 = pst1.executeQuery();

            MessageDto messageDto = null;

            while (rs1.next()) { // no permistion
                messageDto = MessageDto.builder()
                        .messageId(rs1.getString(1))
                        .content(rs1.getString(2))
                        .fileUrl(rs1.getString(3))
                        .channelId(rs1.getString(4))
                        .createdAt(rs1.getString(5))
                        .updatedAt(rs1.getString(6))
                        .author(MemberProfile.builder()
                                .memberId(rs1.getString(7))
                                .role(rs1.getInt(8))
                                .serverId(rs1.getString(9))
                                .joinAt(rs1.getString(10))
                                .profileId(rs1.getString(11))
                                .name(rs1.getString(12))
                                .avtUrl(rs1.getString(13))
                                .build())
                        .build();
            }

            if (messageDto == null) {
                return 0;
            }

            String updatedAt = Instant.now().toString();

            pst2 = con.prepareStatement(updateMessageQuery);
            pst2.setString(1, update.getContent());
            pst2.setString(2, updatedAt);
            pst2.setString(3, update.getMessageId());

            pst2.executeUpdate();

            doCommit(con);

            result = 1;

            messageDto.setContent(update.getContent());

            this.redisMessagePublisher.publish(update.getServerId(), MessageEvent.builder().type("edit").message(messageDto).build().toString());
        } catch (SQLException ex) {
            doRollback(con);
        } finally {
            closeResultSet(rs1, rs2);
            closePreparedStatement(pst1, pst2);
            closeConnection(con);
        }
        return result;
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
    public void deleteMessage(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        String getMemberQuery = "select " +
                "            tbl_member.id, tbl_member.role, tbl_member.server_id, tbl_member.join_at, " +
                "            tbl_profile.id, tbl_profile.name, tbl_profile.avt_url " +
                "            from tbl_member " +
                "            inner join tbl_profile " +
                "            on tbl_member.profile_id = tbl_profile.id " +
                "            where tbl_profile.id = ? " +
                "            and tbl_member.server_id = ?;";
        String getMessageQuery = "select " +
                "            tbl_message.id, tbl_message.content, tbl_message.file_url, tbl_message.channel_id, tbl_message.created_by, tbl_message.created_at, tbl_message.updated_at " +
                "            from tbl_message " +
                "            where tbl_message.id = ? " +
                "            and tbl_message.deleted_at is null;";
        String updateMessageQuery = "update tbl_message set deleted_at = ?, deleted_by = ? where tbl_message.id = ?";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        PreparedStatement pst3 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        int result = 0;

        try {
            con = getConnection();

            pst1 = con.prepareStatement(getMemberQuery);
            pst1.setString(1, delete.getProfileId());
            pst1.setString(2, delete.getServerId());

            rs1 = pst1.executeQuery();

            MemberProfile memberProfile = null;

            while (rs1.next()) { // no permistion
                memberProfile = MemberProfile.builder()
                        .memberId(rs1.getString(1))
                        .role(rs1.getInt(2))
                        .serverId(rs1.getString(3))
                        .joinAt(rs1.getString(4))
                        .profileId(rs1.getString(5))
                        .name(rs1.getString(6))
                        .avtUrl(rs1.getString(7))
                        .build();
            }

            if (memberProfile == null) {
                return 0;
            }

            pst2 = con.prepareStatement(getMessageQuery);
            pst2.setString(1, delete.getMessageId());

            rs2 = pst2.executeQuery();
            Message message = null;

            if (rs2.next()) { // no permistion
                message = Message.builder()
                        .messageId(rs2.getString(1))
                        .content(rs2.getString(2))
                        .fileUrl(rs2.getString(3))
                        .channelId(rs2.getString(4))
                        .createdBy(rs2.getString(5))
                        .createdAt(rs2.getString(6))
                        .updatedAt(rs2.getString(7))
                        .build();
            }

            if (message == null) {
                return 0;
            }

            boolean isOwner = message.getCreatedBy().equals(memberProfile.getProfileId());
            boolean isPermission = Arrays.asList(MemberRole.ADMIN.getNumber(), MemberRole.MODERATOR.getNumber()).contains(memberProfile.getRole());
            boolean canDelete = isOwner || isPermission;

            if (!canDelete) {
                return 0;
            }

            String deletedAt = Instant.now().toString();

            pst3 = con.prepareStatement(updateMessageQuery);
            pst3.setString(1, deletedAt);
            pst3.setString(2, memberProfile.getProfileId());
            pst3.setString(3, delete.getMessageId());

            pst3.executeUpdate();

            doCommit(con);

            result = 1;

            MessageDto messageDto = MessageDto.builder()
                    .messageId(message.getMessageId())
                    .content("This message has been deleted.")
                    .channelId(message.getChannelId())
                    .createdAt(message.getCreatedAt())
                    .updatedAt(message.getUpdatedAt())
                    .deletedAt(deletedAt)
                    .deletedBy(memberProfile.getProfileId())
                    .author(memberProfile)
                    .build();

            this.redisMessagePublisher.publish(delete.getServerId(), MessageEvent.builder().type("edit").message(messageDto).build().toString());
        } catch (SQLException ex) {
            doRollback(con);
        } finally {
            closeResultSet(rs1, rs2);
            closePreparedStatement(pst1, pst2, pst3);
            closeConnection(con);
        }
        return result;
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
    public void getMessagesByChannelId(GrpcRequest request, StreamObserver<GrpcResponse> responseObserver) {
        GrpcGetMessagesByChannelIdRequest req = ServiceHelper.unpackedRequest(request, GrpcGetMessagesByChannelIdRequest.class);

        String isMemberQuery = "select 1 from tbl_member as m where m.profile_id = ? and m.server_id = ?";

        String countQuery = "select count(ms.id) from tbl_message as ms where ms.channel_id = ?";

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
                "            on tbl_profile.id = tbl_member.profile_id " +
                "            where tbl_message.channel_id = ? " +
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

            pst1 = con.prepareStatement(isMemberQuery);
            pst1.setString(1, req.getProfileId());
            pst1.setString(2, req.getServerId());

            rs1 = pst1.executeQuery();

            boolean isMember = rs1.next();

            if (!isMember) { // no permistion
                ServiceHelper.next(responseObserver, ServiceHelper.packedErrorResponse(GrpcErrorCode.ERROR_CODE_NOT_FOUND, "user not found with id"));
                return;
            }

            pst2 = con.prepareStatement(countQuery);

            pst2.setString(1, req.getChannelId());

            rs2 = pst2.executeQuery();

            if (!rs2.next()) {
                GrpcMeta meta = GrpcMeta.newBuilder()
                        .setTotalElements(0)
                        .setTotalPages(0)
                        .setPageNumber(req.getPageNumber())
                        .setPageSize(req.getPageSize())
                        .build();
                builder.setMeta(meta);

                ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(builder.build()));
                return;
            }

            long totalElements = rs2.getLong(1);

            GrpcMeta meta = GrpcMeta.newBuilder()
                    .setTotalElements(totalElements)
                    .setTotalPages(totalElements == 0 ? 1 : (int)Math.ceil((double)totalElements / (double)req.getPageSize()))
                    .setPageNumber(req.getPageNumber())
                    .setPageSize(req.getPageSize())
                    .build();

            builder.setMeta(meta);

            pst3 = con.prepareStatement(messagesQuery);
            pst3.setString(1, req.getChannelId());
            pst3.setInt(2, req.getPageSize());
            pst3.setInt(3, req.getPageNumber() * req.getPageSize());

            rs3 = pst3.executeQuery();

            while (rs3.next()) {
                GrpcMemberProfile memberProfile = GrpcMemberProfile.newBuilder()
                        .setMemberId(rs1.getString(9))
                        .setRole(GrpcMemberRole.forNumber(rs1.getInt(10)))
                        .setServerId(rs1.getString(11))
                        .setJoinAt(rs1.getString(12))
                        .setProfileId(rs1.getString(13))
                        .setName(rs1.getString(14))
                        .setAvtUrl(rs1.getString(15))
                        .build();

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
                builderMessage.setAuthor(memberProfile);

                GrpcMessage message = builderMessage.build();

                builder.addContent(message);
            }

            ServiceHelper.next(responseObserver, ServiceHelper.packedSuccessResponse(builder.build()));
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            mysteryJdbc.closeResultSet(rs1, rs2, rs3);
            mysteryJdbc.closePreparedStatement(pst1, pst2, pst3);
        }
    }
}
