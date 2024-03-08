package com.vuong.app.service;

import com.vuong.app.jdbc.JdbcUtils;
import com.vuong.app.jdbc.JdbcClient;
import com.vuong.app.jdbc.SqlSession;
import com.vuong.app.jdbc.exception.JdbcDataAccessException;
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
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class ServerService extends ServerServiceGrpc.ServerServiceImplBase {

    private final MessagePublisher messagePublisher;

    private final SqlSession sqlSession;

    @Override
    public void createServer(GrpcCreateServerRequest request, StreamObserver<GrpcCreateServerResponse> responseObserver) {
        final String EXISTS_PROFILE_QUERY = "select 1 from tbl_profile as p where p.id = ?";
        final String INSERT_SERVER_QUERY = "insert into tbl_server(id, name, img_url, invite_code, created_by, created_at, updated_at) values(?, ?, ?, ?, ?, ?, ?)";
        final String INSERT_CHANNEL_QUERY = "insert into tbl_channel(id, name, type, server_id, created_at, updated_at, updated_by) values(?, ?, ?, ?, ?, ?, ?)";
        final String INSERT_MEMBER_QUERY = "insert into tbl_member(id, role, profile_id, server_id, join_at) values (?, ?, ?, ?, ?)";

        try {
            sqlSession.openSession();

            boolean existsProfile = JdbcClient.sql(EXISTS_PROFILE_QUERY)
                    .setString(1, request.getAuthorId())
                    .exists();

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

            JdbcClient.sql(INSERT_SERVER_QUERY)
                    .setString(1, serverId)
                    .setString(2, request.getName())
                    .setString(3, request.getImgUrl())
                    .setString(4, serverId)
                    .setString(5, request.getAuthorId())
                    .setString(6, now.toString())
                    .setString(7, now.toString())
                    .insert();

            String channelId = UUID.randomUUID().toString();
            JdbcClient.sql(INSERT_CHANNEL_QUERY)
                    .setString(1, channelId)
                    .setString(2, "general")
                    .setInt(3, GrpcChannelType.CHANNEL_TYPE_TEXT_VALUE)
                    .setString(4, serverId)
                    .setString(5, now.toString())
                    .setString(6, now.toString())
                    .setString(7, request.getAuthorId())
                    .insert();

            String memberId = UUID.randomUUID().toString();
            JdbcClient.sql(INSERT_MEMBER_QUERY)
                    .setString(1, memberId)
                    .setInt(2, GrpcMemberRole.MEMBER_ROLE_ADMIN_VALUE)
                    .setString(3, request.getAuthorId())
                    .setString(4, serverId)
                    .setString(5, now.toString())
                    .insert();

            sqlSession.commit();

            GrpcCreateServerResponse response = GrpcCreateServerResponse.newBuilder()
                    .setServerId(serverId)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (JdbcDataAccessException ex) {
            sqlSession.rollback();
        } finally {
            sqlSession.closeConnection();
        }
    }

    @Override
    public void getServersJoin(GrpcGetServersJoinRequest request, StreamObserver<GrpcGetServersJoinResponse> responseObserver) {
        final String COUNT_QUERY = "select count(m.server_id) from tbl_member as m where m.profile_id = ?";

        final String SERVER_ID_JOIN_QUERY = "select m.server_id as member_server_id from tbl_member as m where m.profile_id = ? order by m.join_at asc limit ? offset ?";
        final String SERVER_QUERY = "select " +
                "s.id as server_id, s.name as server_name, s.img_url as server_img_url, " +
                "s.invite_code as server_invite_code, s.created_by as server_created_by, " +
                "s.created_at as server_created_at, s.updated_at as server_updated_at " +
                "from tbl_server as s inner join (" + SERVER_ID_JOIN_QUERY + ") as sm " +
                "on s.id = sm.member_server_id";

//        limit not work on subquery "in"

        GrpcGetServersJoinResponse.Builder builder = GrpcGetServersJoinResponse.newBuilder();

        try {
            sqlSession.openSession();

            long totalElements = JdbcClient.sql(COUNT_QUERY)
                    .setString(1, request.getProfileId())
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

            JdbcClient.sql(SERVER_QUERY)
                    .setString(1, request.getProfileId())
                    .setInt(2, request.getPageSize())
                    .setInt(3, request.getPageNumber() * request.getPageSize())
                    .query(rs -> {
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
                    });

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } finally {
            sqlSession.closeConnection();
        }
    }

    @Override
    public void getFirstServerJoin(GrpcGetFirstServerJoinRequest request, StreamObserver<GrpcGetFirstServerJoinResponse> responseObserver) {
        final String FIRST_SERVER_ID_JOIN_QUERY = "select m.server_id from tbl_member as m where m.profile_id = ? order by m.join_at limit 1";
        final String SERVER_QUERY = "select " +
                "s.id as server_id, s.name as server_name, s.img_url as server_img_url, " +
                "s.invite_code as server_invite_code, s.created_by as server_created_by, " +
                "s.created_at as server_created_at, s.updated_at as server_updated_at " +
                "from tbl_server as s where s.id = (" + FIRST_SERVER_ID_JOIN_QUERY + ")";

        try {
            sqlSession.openSession();

            GrpcServer grpcServer = JdbcClient.sql(SERVER_QUERY)
                    .setString(1, request.getProfileId())
                    .query(rs -> {
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
                    });

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
        } finally {
            sqlSession.closeConnection();
        }
    }

    @Override
    public void getServerJoinByServerId(GrpcGetServerJoinByServerIdRequest request, StreamObserver<GrpcGetServerJoinByServerIdResponse> responseObserver) {
        final String IS_MEMBER_QUERY = "exists (select 1 from tbl_member as m where m.profile_id = ? and m.server_id = ?)";
        final String SERVER_QUERY = "select " +
                "s.id as server_id, s.name as server_name, s.img_url as server_img_url, " +
                "s.invite_code as server_invite_code, s.created_by as server_created_by, " +
                "s.created_at as server_created_at, s.updated_at as server_updated_at " +
                "from tbl_server as s where s.id = ? and " + IS_MEMBER_QUERY;

        try {
            sqlSession.openSession();

            GrpcServer grpcServer = JdbcClient.sql(SERVER_QUERY)
                    .setString(1, request.getServerId())
                    .setString(2, request.getProfileId())
                    .setString(3, request.getServerId())
                    .query(rs -> {
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
                    });

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
        } finally {
            sqlSession.closeConnection();
        }
    }

    @Override
    public void getServerJoinIds(GrpcGetServerJoinIdsRequest request, StreamObserver<GrpcGetServerJoinIdsResponse> responseObserver) {
        String SERVER_JOIN_IDS_QUERY = "select tbl_member.server_id from tbl_member where tbl_member.profile_id = ?";

        try {
            sqlSession.openSession();

            GrpcGetServerJoinIdsResponse.Builder builder = GrpcGetServerJoinIdsResponse.newBuilder();

            JdbcClient.sql(SERVER_JOIN_IDS_QUERY)
                    .setString(1, request.getProfileId())
                    .query(rs -> {
                        while (rs.next()) {
                            builder.addResult(rs.getString(1));
                        }
                        return null;
                    });

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } finally {
            sqlSession.closeConnection();
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
            sqlSession.openSession();

            String serverId = JdbcClient.sql(EXIST_JOIN_SERVER_QUERY)
                    .setString(1, request.getInviteCode())
                    .setString(2, request.getProfileId())
                    .query(rs -> {
                        while (rs.next()) {
                            return rs.getString(1);
                        }
                        return null;
                    });

            GrpcJoinServerByInviteCodeResponse.Builder builder = GrpcJoinServerByInviteCodeResponse.newBuilder();

            if (serverId != null) {
                builder.setServerId(serverId);
                responseObserver.onNext(builder.build());
                responseObserver.onCompleted();
            }

            serverId = JdbcClient.sql(SERVER_ID_QUERY)
                    .setString(1, request.getInviteCode())
                    .query(rs -> {
                        while (rs.next()) {
                            return rs.getString(1);
                        }

                        return null;
                    });

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

            JdbcClient.sql(JOIN_SERVER_QUERY)
                    .setString(1, memberId)
                    .setInt(2, GrpcMemberRole.MEMBER_ROLE_GUEST_VALUE)
                    .setString(3, request.getProfileId())
                    .setString(4, serverId)
                    .setString(5, now.toString())
                    .update();

            sqlSession.commit();

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
        } catch (JdbcDataAccessException ex) {
            sqlSession.rollback();
        } finally {
            sqlSession.closeConnection();
        }
    }

    @Override
    public void leaveServer(GrpcLeaveServerRequest request, StreamObserver<GrpcLeaveServerResponse> responseObserver) {
        String MEMBER_ID_QUERY = "select tbl_member.id from tbl_member where tbl_member.server_id = ? and tbl_member.profile_id = ? and tbl_member.role <> ?";

        String LEAVE_SERVER_QUERY = "delete from tbl_member where tbl_member.id = ?";

        try {
            sqlSession.openSession();

            String memberId = JdbcClient.sql(MEMBER_ID_QUERY)
                    .setString(1, request.getServerId())
                    .setString(2, request.getProfileId())
                    .setInt(3, GrpcMemberRole.MEMBER_ROLE_ADMIN_VALUE)
                    .query(rs -> {
                        while (rs.next()) {
                            return rs.getString(1);
                        }
                        return null;
                    });

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

            JdbcClient.sql(LEAVE_SERVER_QUERY)
                    .setString(1, memberId)
                    .delete();

            sqlSession.commit();

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
        } catch (JdbcDataAccessException ex) {
            sqlSession.rollback();
        } finally {
            sqlSession.closeConnection();
        }
    }

    @Override
    public void getServerIds(GrpcGetServerIdsRequest request, StreamObserver<GrpcGetServerIdsResponse> responseObserver) {

    }
}
