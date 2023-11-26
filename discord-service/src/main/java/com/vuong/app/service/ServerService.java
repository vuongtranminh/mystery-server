package com.vuong.app.service;

import com.vuong.app.config.MysteryJdbc;
import com.vuong.app.v1.GrpcErrorCode;
import com.vuong.app.v1.GrpcErrorResponse;
import com.vuong.app.v1.GrpcMeta;
import com.vuong.app.v1.discord.*;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class ServerService extends ServerServiceGrpc.ServerServiceImplBase {

    private final MysteryJdbc mysteryJdbc;

    @Override
    public void createServer(GrpcCreateServerRequest request, StreamObserver<GrpcCreateServerResponse> responseObserver) {
        String existsProfileQuery = "select 1 from tbl_profile as p where p.id = ?";
        String insertServerQuery = "insert into tbl_server(id, name, img_url, invite_code, created_by, created_at, updated_at) values(?, ?, ?, ?, ?, ?, ?)";
        String insertChannelQuery = "insert into tbl_channel(id, name, type, server_id, created_at, updated_at, updated_by) values(?, ?, ?, ?, ?, ?, ?)";
        String insertMemberQuery = "insert into tbl_member(id, role, profile_id, server_id, join_at) values (?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        PreparedStatement pst3 = null;
        PreparedStatement pst4 = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();

            pst1 = con.prepareStatement(existsProfileQuery);
            pst1.setString(1, request.getAuthorId());
            rs = pst1.executeQuery();
            boolean existsProfile = rs.next();

            if (!existsProfile) {
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

            pst2 = con.prepareStatement(insertServerQuery);
            String serverId = UUID.randomUUID().toString();

            pst2.setString(1, serverId);
            pst2.setString(2, request.getName());
            pst2.setString(3, request.getImgUrl());
            pst2.setString(4, serverId);
            pst2.setString(5, request.getAuthorId());
            pst2.setString(6, Instant.now().toString());
            pst2.setString(7, Instant.now().toString());

            pst3 = con.prepareStatement(insertChannelQuery);
            String channelId = UUID.randomUUID().toString();

            pst3.setString(1, channelId);
            pst3.setString(2, "general");
            pst3.setInt(3, GrpcChannelType.CHANNEL_TYPE_TEXT_VALUE);
            pst3.setString(4, serverId);
            pst3.setString(5, Instant.now().toString());
            pst3.setString(6, Instant.now().toString());
            pst3.setString(7, request.getAuthorId());

            pst4 = con.prepareStatement(insertMemberQuery);
            String memberId = UUID.randomUUID().toString();

            pst4.setString(1, memberId);
            pst4.setInt(2, GrpcMemberRole.MEMBER_ROLE_ADMIN_VALUE);
            pst4.setString(3, request.getAuthorId());
            pst4.setString(4, serverId);
            pst4.setString(5, Instant.now().toString());

            mysteryJdbc.setAutoCommit(false);
            pst2.executeUpdate();
            pst3.executeUpdate();
            pst4.executeUpdate();

            mysteryJdbc.doCommit();

            GrpcCreateServerResponse response = GrpcCreateServerResponse.newBuilder()
                    .setServerId(serverId)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst1, pst2, pst3, pst4);
            mysteryJdbc.setAutoCommit(true);
        }
    }

    @Override
    public void getServersJoin(GrpcGetServersJoinRequest request, StreamObserver<GrpcGetServersJoinResponse> responseObserver) {
        String countQuery = "select count(m.server_id) from tbl_member as m where m.profile_id = ?";

        String serverIdJoinQuery = "select m.server_id as member_server_id from tbl_member as m where m.profile_id = ? order by m.join_at asc limit ? offset ?";
        String serverQuery = "select " +
                "s.id as server_id, s.name as server_name, s.img_url as server_img_url, " +
                "s.invite_code as server_invite_code, s.created_by as server_created_by, " +
                "s.created_at as server_created_at, s.updated_at as server_updated_at " +
                "from tbl_server as s inner join (" + serverIdJoinQuery + ") as sm " +
                "on s.id = sm.member_server_id";

//        limit not work on subquery "in"

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        GrpcGetServersJoinResponse.Builder builder = GrpcGetServersJoinResponse.newBuilder();

        try {
            con = mysteryJdbc.getConnection();

            pst1 = con.prepareStatement(countQuery);
            pst1.setString(1, request.getProfileId());
            rs1 = pst1.executeQuery();

            long totalElements = 0;

            while (rs1.next()) {
                totalElements = rs1.getLong(1);
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

            pst2 = con.prepareStatement(serverQuery);
            pst2.setString(1, request.getProfileId());
            pst2.setInt(2, request.getPageSize());
            pst2.setInt(3, request.getPageNumber() * request.getPageSize());

            rs2 = pst2.executeQuery();

            while (rs2.next()) {
                builder.addContent(GrpcServer.newBuilder()
                        .setServerId(rs2.getString(1))
                        .setName(rs2.getString(2))
                        .setImgUrl(rs2.getString(3))
                        .setInviteCode(rs2.getString(4))
                        .setAuthorId(rs2.getString(5))
                        .setCreatedAt(rs2.getString(6))
                        .setUpdatedAt(rs2.getString(7))
                        .build());
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            mysteryJdbc.closeResultSet(rs1, rs2);
            mysteryJdbc.closePreparedStatement(pst1, pst2);
        }
    }

    @Override
    public void getFirstServerJoin(GrpcGetFirstServerJoinRequest request, StreamObserver<GrpcGetFirstServerJoinResponse> responseObserver) {
        String firstServerIdJoinQuery = "select m.server_id from tbl_member as m where m.profile_id = ? order by m.join_at limit 1";
        String serverQuery = "select " +
                "s.id as server_id, s.name as server_name, s.img_url as server_img_url, " +
                "s.invite_code as server_invite_code, s.created_by as server_created_by, " +
                "s.created_at as server_created_at, s.updated_at as server_updated_at " +
                "from tbl_server as s where s.id = (" + firstServerIdJoinQuery + ")";

        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(serverQuery);
            pst.setString(1, request.getProfileId());

            rs = pst.executeQuery();

            GrpcServer grpcServer = null;

            while (rs.next()) {
                grpcServer = GrpcServer.newBuilder()
                        .setServerId(rs.getString(1))
                        .setName(rs.getString(2))
                        .setImgUrl(rs.getString(3))
                        .setInviteCode(rs.getString(4))
                        .setAuthorId(rs.getString(5))
                        .setCreatedAt(rs.getString(6))
                        .setUpdatedAt(rs.getString(7))
                        .build();
            }

            if (grpcServer == null) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_NOT_FOUND;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not has first server join")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException(metadata));
                return;
            }

            GrpcGetFirstServerJoinResponse response = GrpcGetFirstServerJoinResponse.newBuilder()
                    .setResult(grpcServer)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    @Override
    public void getServerJoinByServerId(GrpcGetServerJoinByServerIdRequest request, StreamObserver<GrpcGetServerJoinByServerIdResponse> responseObserver) {
        String isMemberQuery = "exists (select 1 from tbl_member as m where m.profile_id = ? and m.server_id = ?)";
        String serverQuery = "select " +
                "s.id as server_id, s.name as server_name, s.img_url as server_img_url, " +
                "s.invite_code as server_invite_code, s.created_by as server_created_by, " +
                "s.created_at as server_created_at, s.updated_at as server_updated_at " +
                "from tbl_server as s where s.id = ? and " + isMemberQuery;

        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(serverQuery);
            pst.setString(1, request.getServerId());
            pst.setString(2, request.getProfileId());
            pst.setString(3, request.getServerId());

            rs = pst.executeQuery();

            GrpcServer grpcServer = null;

            while (rs.next()) {
                grpcServer = GrpcServer.newBuilder()
                        .setServerId(rs.getString(1))
                        .setName(rs.getString(2))
                        .setImgUrl(rs.getString(3))
                        .setInviteCode(rs.getString(4))
                        .setAuthorId(rs.getString(5))
                        .setCreatedAt(rs.getString(6))
                        .setUpdatedAt(rs.getString(7))
                        .build();
            }

            if (grpcServer == null) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_NOT_FOUND;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not has first server join")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException(metadata));
                return;
            }

            GrpcGetServerJoinByServerIdResponse response = GrpcGetServerJoinByServerIdResponse.newBuilder()
                    .setResult(grpcServer)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    @Override
    public void getServerJoinIds(GrpcGetServerJoinIdsRequest request, StreamObserver<GrpcGetServerJoinIdsResponse> responseObserver) {
        String serverJoinIdsQuery = "select tbl_member.server_id from tbl_member where tbl_member.profile_id = ?";

        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(serverJoinIdsQuery);
            pst.setString(1, request.getProfileId());
            rs = pst.executeQuery();

            GrpcGetServerJoinIdsResponse.Builder builder = GrpcGetServerJoinIdsResponse.newBuilder();

            while (rs.next()) {
                builder.addResult(rs.getString(1));
            }

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    @Override
    public void joinServerByInviteCode(GrpcJoinServerByInviteCodeRequest request, StreamObserver<GrpcJoinServerByInviteCodeResponse> responseObserver) {
        String existJoinServerQuery = "select tbl_server.id " +
                "from tbl_server inner join tbl_member " +
                "on tbl_server.id = tbl_member.id " +
                "where tbl_server.invite_code = ? and tbl_member.profile_id = ?";

//        String joinServerQuery = "insert into tbl_member(id, role, profile_id, server_id, join_at) " +
//                "select ?, ?, ?, tbl_server.id, ? from tbl_server where tbl_server.invite_code = ?";
        String serverIdQuery = "select tbl_server.id from tbl_server where tbl_server.invite_code = ?";
        String joinServerQuery = "insert into tbl_member(id, role, profile_id, server_id, join_at) values (?, ?, ?, ?, ?)";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        PreparedStatement pst3 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        try {
            con = mysteryJdbc.getConnection();

            pst1 = con.prepareStatement(existJoinServerQuery);
            pst1.setString(1, request.getInviteCode());
            pst1.setString(2, request.getProfileId());
            rs1 = pst1.executeQuery();

            String serverId = null;
            GrpcJoinServerByInviteCodeResponse.Builder builder = GrpcJoinServerByInviteCodeResponse.newBuilder();
            while (rs1.next()) {
                serverId = rs1.getString(1);
            }

            if (serverId != null) {
                builder.setServerId(serverId);
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
            }

            pst2 = con.prepareStatement(serverIdQuery);
            pst2.setString(1, request.getInviteCode());
            rs2 = pst2.executeQuery();

            while (rs2.next()) {
                serverId = rs2.getString(1);
            }

            if (serverId == null) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_NOT_FOUND;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not found server with invite code")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException(metadata));
                return;
            }

            String memberId = UUID.randomUUID().toString();
            pst3 = con.prepareStatement(joinServerQuery);
            pst3.setString(1, memberId);
            pst3.setInt(2, GrpcMemberRole.MEMBER_ROLE_GUEST_VALUE);
            pst3.setString(3, request.getProfileId());
            pst3.setString(4, serverId);
            pst3.setString(5, Instant.now().toString());

            int result = pst3.executeUpdate();

            builder.setServerId(serverId);

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            ex.printStackTrace();
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs1, rs2);
            mysteryJdbc.closePreparedStatement(pst1, pst2, pst3);
        }
    }

    @Override
    public void leaveServer(GrpcLeaveServerRequest request, StreamObserver<GrpcLeaveServerResponse> responseObserver) {
        String memberIdQuery = "select tbl_member.id from tbl_member where tbl_member.server_id = ? and tbl_member.profile_id = ? and tbl_member.role <> ?";

        String leaveServerQuery = "delete from tbl_member where tbl_member.id = ?";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();

            pst1 = con.prepareStatement(memberIdQuery);
            pst1.setString(1, request.getServerId());
            pst1.setString(2, request.getProfileId());
            pst1.setInt(3, GrpcMemberRole.MEMBER_ROLE_ADMIN_VALUE);
            rs = pst1.executeQuery();

            if (!mysteryJdbc.hasResult(rs)) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_NOT_FOUND;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not found member")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.NOT_FOUND.asRuntimeException(metadata));
                return;
            }

            String memberId = null;
            while (rs.next()) {
                memberId = rs.getString(1);
            }

            pst2 = con.prepareStatement(leaveServerQuery);
            pst2.setString(1, memberId);
            int result = pst2.executeUpdate();

            GrpcLeaveServerResponse response = GrpcLeaveServerResponse.newBuilder()
                    .setMemberId(memberId)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            ex.printStackTrace();
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst1, pst2);
        }
    }
}
