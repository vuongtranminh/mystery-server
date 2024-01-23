package com.vuong.app.service;

import com.vuong.app.config.JdbcTemplate;
import com.vuong.app.config.JdbcUtils;
import com.vuong.app.redis.MessagePublisher;
import com.vuong.app.v1.GrpcErrorCode;
import com.vuong.app.v1.GrpcErrorResponse;
import com.vuong.app.v1.GrpcMeta;
import com.vuong.app.v1.discord.*;
import com.vuong.app.v1.event.GrpcEvent;
import com.vuong.app.v1.event.GrpcMemberEvent;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
public class ServerService extends ServerServiceGrpc.ServerServiceImplBase {

    private final DataSource dataSource;
    private final MessagePublisher messagePublisher;

    @Override
    public void createServer(GrpcCreateServerRequest request, StreamObserver<GrpcCreateServerResponse> responseObserver) {
        final String EXISTS_PROFILE_QUERY = "select 1 from tbl_profile as p where p.id = ?";
        final String INSERT_SERVER_QUERY = "insert into tbl_server(id, name, img_url, invite_code, created_by, created_at, updated_at) values(?, ?, ?, ?, ?, ?, ?)";
        final String INSERT_CHANNEL_QUERY = "insert into tbl_channel(id, name, type, server_id, created_at, updated_at, updated_by) values(?, ?, ?, ?, ?, ?, ?)";
        final String INSERT_MEMBER_QUERY = "insert into tbl_member(id, role, profile_id, server_id, join_at) values (?, ?, ?, ?, ?)";

        try {
            JdbcUtils.openSession(dataSource);

            boolean existsProfile = JdbcTemplate.exists(
                    EXISTS_PROFILE_QUERY,
                    List.of(request.getAuthorId())
            );

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

            Instant now = Instant.now();

            String serverId = UUID.randomUUID().toString();
            JdbcTemplate.create(
                    INSERT_SERVER_QUERY,
                    List.of(
                            serverId,
                            request.getName(),
                            request.getImgUrl(),
                            serverId,
                            request.getAuthorId(),
                            now.toString(),
                            now.toString()
                    )
            );

            String channelId = UUID.randomUUID().toString();
            JdbcTemplate.create(
                    INSERT_CHANNEL_QUERY,
                    List.of(
                            channelId,
                            "general",
                            GrpcChannelType.CHANNEL_TYPE_TEXT_VALUE,
                            serverId,
                            now.toString(),
                            now.toString(),
                            request.getAuthorId()
                    )
            );

            String memberId = UUID.randomUUID().toString();
            JdbcTemplate.create(
                    INSERT_MEMBER_QUERY,
                    List.of(
                            memberId,
                            GrpcMemberRole.MEMBER_ROLE_ADMIN_VALUE,
                            request.getAuthorId(),
                            serverId,
                            now.toString()
                    )
            );

            JdbcUtils.doCommit();

            GrpcCreateServerResponse response = GrpcCreateServerResponse.newBuilder()
                    .setServerId(serverId)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            JdbcUtils.doRollback();
        } finally {
            JdbcUtils.closeConnection();
        }
    }

    @Override
    public void getServersJoin(GrpcGetServersJoinRequest request, StreamObserver<GrpcGetServersJoinResponse> responseObserver) {
        String COUNT_QUERY = "select count(m.server_id) from tbl_member as m where m.profile_id = ?";

        String SERVER_ID_JOIN_QUERY = "select m.server_id as member_server_id from tbl_member as m where m.profile_id = ? order by m.join_at asc limit ? offset ?";
        String SERVER_QUERY = "select " +
                "s.id as server_id, s.name as server_name, s.img_url as server_img_url, " +
                "s.invite_code as server_invite_code, s.created_by as server_created_by, " +
                "s.created_at as server_created_at, s.updated_at as server_updated_at " +
                "from tbl_server as s inner join (" + SERVER_ID_JOIN_QUERY + ") as sm " +
                "on s.id = sm.member_server_id";

//        limit not work on subquery "in"

        GrpcGetServersJoinResponse.Builder builder = GrpcGetServersJoinResponse.newBuilder();

        try {

            JdbcUtils.initConnection(dataSource);

            long totalElements = JdbcTemplate.count(
                    COUNT_QUERY,
                    List.of(
                            request.getProfileId()
                    )
            );

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

            JdbcTemplate.query(
                    SERVER_QUERY,
                    List.of(
                            request.getProfileId(),
                            request.getPageSize(),
                            request.getPageNumber() * request.getPageSize()
                    ),
                    rs -> {
                        while (rs.next()) {
                            builder.addContent(GrpcServer.newBuilder()
                                    .setServerId(rs.getString(1))
                                    .setName(rs.getString(2))
                                    .setImgUrl(rs.getString(3))
                                    .setInviteCode(rs.getString(4))
                                    .setAuthorId(rs.getString(5))
                                    .setCreatedAt(rs.getString(6))
                                    .setUpdatedAt(rs.getString(7))
                                    .build());
                        }
                        return null;
                    }
            );

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            JdbcUtils.closeConnection();
        }
    }

    @Override
    public void getFirstServerJoin(GrpcGetFirstServerJoinRequest request, StreamObserver<GrpcGetFirstServerJoinResponse> responseObserver) {
        String FIRST_SERVER_ID_JOIN_QUERY = "select m.server_id from tbl_member as m where m.profile_id = ? order by m.join_at limit 1";
        String SERVER_QUERY = "select " +
                "s.id as server_id, s.name as server_name, s.img_url as server_img_url, " +
                "s.invite_code as server_invite_code, s.created_by as server_created_by, " +
                "s.created_at as server_created_at, s.updated_at as server_updated_at " +
                "from tbl_server as s where s.id = (" + FIRST_SERVER_ID_JOIN_QUERY + ")";

        try {
            JdbcUtils.initConnection(dataSource);

            GrpcServer grpcServer = JdbcTemplate.query(
                    SERVER_QUERY,
                    List.of(
                            request.getProfileId()
                    ),
                    rs -> {
                        while (rs.next()) {
                            return GrpcServer.newBuilder()
                                    .setServerId(rs.getString(1))
                                    .setName(rs.getString(2))
                                    .setImgUrl(rs.getString(3))
                                    .setInviteCode(rs.getString(4))
                                    .setAuthorId(rs.getString(5))
                                    .setCreatedAt(rs.getString(6))
                                    .setUpdatedAt(rs.getString(7))
                                    .build();
                        }

                        return null;
                    }
            );

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
            JdbcUtils.closeConnection();
        }
    }

    @Override
    public void getServerJoinByServerId(GrpcGetServerJoinByServerIdRequest request, StreamObserver<GrpcGetServerJoinByServerIdResponse> responseObserver) {
        String IS_MEMBER_QUERY = "exists (select 1 from tbl_member as m where m.profile_id = ? and m.server_id = ?)";
        String SERVER_QUERY = "select " +
                "s.id as server_id, s.name as server_name, s.img_url as server_img_url, " +
                "s.invite_code as server_invite_code, s.created_by as server_created_by, " +
                "s.created_at as server_created_at, s.updated_at as server_updated_at " +
                "from tbl_server as s where s.id = ? and " + IS_MEMBER_QUERY;

        try {
            JdbcUtils.initConnection(dataSource);

            GrpcServer grpcServer = JdbcTemplate.query(
                    SERVER_QUERY,
                    List.of(
                            request.getServerId(),
                            request.getProfileId(),
                            request.getServerId()
                    ),
                    rs -> {
                        while (rs.next()) {
                            return GrpcServer.newBuilder()
                                    .setServerId(rs.getString(1))
                                    .setName(rs.getString(2))
                                    .setImgUrl(rs.getString(3))
                                    .setInviteCode(rs.getString(4))
                                    .setAuthorId(rs.getString(5))
                                    .setCreatedAt(rs.getString(6))
                                    .setUpdatedAt(rs.getString(7))
                                    .build();
                        }
                        return null;
                    }
            );

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
            JdbcUtils.closeConnection();
        }
    }

    @Override
    public void getServerJoinIds(GrpcGetServerJoinIdsRequest request, StreamObserver<GrpcGetServerJoinIdsResponse> responseObserver) {
        String SERVER_JOIN_IDS_QUERY = "select tbl_member.server_id from tbl_member where tbl_member.profile_id = ?";

        try {
            JdbcUtils.initConnection(dataSource);

            GrpcGetServerJoinIdsResponse.Builder builder = GrpcGetServerJoinIdsResponse.newBuilder();

            JdbcTemplate.query(
                    SERVER_JOIN_IDS_QUERY,
                    List.of(
                            request.getProfileId()
                    ),
                    rs -> {
                        while (rs.next()) {
                            builder.addResult(rs.getString(1));
                        }
                        return null;
                    }
            );

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            JdbcUtils.closeConnection();
        }
    }

    @Override
    public void joinServerByInviteCode(GrpcJoinServerByInviteCodeRequest request, StreamObserver<GrpcJoinServerByInviteCodeResponse> responseObserver) {
        String EXIST_JOIN_SERVER_QUERY = "select tbl_server.id " +
                "from tbl_server inner join tbl_member " +
                "on tbl_server.id = tbl_member.id " +
                "where tbl_server.invite_code = ? and tbl_member.profile_id = ?";

//        String joinServerQuery = "insert into tbl_member(id, role, profile_id, server_id, join_at) " +
//                "select ?, ?, ?, tbl_server.id, ? from tbl_server where tbl_server.invite_code = ?";
        String SERVER_ID_QUERY = "select tbl_server.id from tbl_server where tbl_server.invite_code = ?";
        String JOIN_SERVER_QUERY = "insert into tbl_member(id, role, profile_id, server_id, join_at) values (?, ?, ?, ?, ?)";

        try {

            JdbcUtils.openSession(dataSource);

            String serverId = JdbcTemplate.query(
                    EXIST_JOIN_SERVER_QUERY,
                    List.of(
                            request.getInviteCode(),
                            request.getProfileId()
                    ),
                    rs -> {
                        while (rs.next()) {
                            return rs.getString(1);
                        }
                        return null;
                    }
            );

            GrpcJoinServerByInviteCodeResponse.Builder builder = GrpcJoinServerByInviteCodeResponse.newBuilder();

            if (serverId != null) {
                builder.setServerId(serverId);
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
            }

            serverId = JdbcTemplate.query(
                    SERVER_ID_QUERY,
                    List.of(
                            request.getInviteCode()
                    ),
                    rs -> {
                        while (rs.next()) {
                            return rs.getString(1);
                        }

                        return null;
                    }
            );

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
            Instant now = Instant.now();

            JdbcTemplate.update(
                    JOIN_SERVER_QUERY,
                    List.of(
                            memberId,
                            GrpcMemberRole.MEMBER_ROLE_GUEST_VALUE,
                            request.getProfileId(),
                            serverId,
                            now.toString()
                    )
            );

            JdbcUtils.doCommit();

            GrpcMemberEvent.GrpcAddMemberEvent grpcAddMemberEvent = GrpcMemberEvent.GrpcAddMemberEvent.newBuilder()
                    .build();

            GrpcMemberEvent grpcMemberEvent = GrpcMemberEvent.newBuilder()
                    .setAddMemberEvent(grpcAddMemberEvent)
                    .build();

            GrpcEvent grpcEvent = GrpcEvent.newBuilder()
                    .setMemberEvent(grpcMemberEvent)
                    .build();

            this.messagePublisher.publish(serverId, grpcEvent);

            builder.setServerId(serverId);

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JdbcUtils.doRollback();
        } finally {
            JdbcUtils.closeConnection();
        }
    }

    @Override
    public void leaveServer(GrpcLeaveServerRequest request, StreamObserver<GrpcLeaveServerResponse> responseObserver) {
        String MEMBER_ID_QUERY = "select tbl_member.id from tbl_member where tbl_member.server_id = ? and tbl_member.profile_id = ? and tbl_member.role <> ?";

        String LEAVE_SERVER_QUERY = "delete from tbl_member where tbl_member.id = ?";

        try {

            JdbcUtils.openSession(dataSource);

            String memberId = JdbcTemplate.query(
                    MEMBER_ID_QUERY,
                    List.of(
                            request.getServerId(),
                            request.getProfileId(),
                            GrpcMemberRole.MEMBER_ROLE_ADMIN_VALUE
                    ),
                    rs -> {
                        while (rs.next()) {
                            return rs.getString(1);
                        }
                        return null;
                    }
            );

            if (memberId == null) {
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

            JdbcTemplate.delete(
                    LEAVE_SERVER_QUERY,
                    List.of(
                            memberId
                    )
            );

            JdbcUtils.doCommit();

            GrpcMemberEvent.GrpcDeleteMemberEvent grpcDeleteMemberEvent = GrpcMemberEvent.GrpcDeleteMemberEvent.newBuilder()
                    .setServerId(request.getServerId())
                    .setMemberId(memberId)
                    .build();

            GrpcMemberEvent grpcMemberEvent = GrpcMemberEvent.newBuilder()
                    .setType(GrpcMemberEvent.GrpcMemberEventType.MEMBER_EVENT_DELETE)
                    .setDeleteMemberEvent(grpcDeleteMemberEvent)
                    .build();

            GrpcEvent grpcEvent = GrpcEvent.newBuilder()
                    .setMemberEvent(grpcMemberEvent)
                    .build();

            this.messagePublisher.publish(request.getServerId(), grpcEvent);

            GrpcLeaveServerResponse response = GrpcLeaveServerResponse.newBuilder()
                    .setMemberId(memberId)
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
}
