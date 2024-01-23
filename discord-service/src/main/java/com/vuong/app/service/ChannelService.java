package com.vuong.app.service;

import com.vuong.app.config.JdbcTemplate;
import com.vuong.app.config.JdbcUtils;
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
public class ChannelService extends ChannelServiceGrpc.ChannelServiceImplBase {

    private final DataSource dataSource;

    @Override
    public void createChannel(GrpcCreateChannelRequest request, StreamObserver<GrpcCreateChannelResponse> responseObserver) {
        // server delete is member delete and channel delete
        String IS_PERMISSION_QUERY = "select 1 from tbl_member as m where m.server_id = ? and m.profile_id = ? and m.role in (?, ?)";
        String INSERT_CHANNEL_QUERY = "insert into tbl_channel(id, name, type, server_id, created_at, updated_at, updated_by) values(?, ?, ?, ?, ?, ?, ?)";
        // note: insert not working with where

        try {

            JdbcUtils.openSession(dataSource);

            boolean hasPermistion = JdbcTemplate.exists(
                    IS_PERMISSION_QUERY,
                    List.of(
                            request.getServerId(),
                            request.getProfileId(),
                            GrpcMemberRole.MEMBER_ROLE_ADMIN_VALUE,
                            GrpcMemberRole.MEMBER_ROLE_MODERATOR_VALUE
                    )
            );

            if (!hasPermistion) {
                Metadata metadata = new Metadata();
                Metadata.Key<GrpcErrorResponse> responseKey = ProtoUtils.keyForProto(GrpcErrorResponse.getDefaultInstance());
                GrpcErrorCode errorCode = GrpcErrorCode.ERROR_CODE_PERMISSION_DENIED;
                GrpcErrorResponse errorResponse = GrpcErrorResponse.newBuilder()
                        .setErrorCode(errorCode)
                        .setMessage("not has first server join")
                        .build();
                // pass the error object via metadata
                metadata.put(responseKey, errorResponse);
                responseObserver.onError(Status.PERMISSION_DENIED.asRuntimeException(metadata));
                return;
            }

            String channelId = UUID.randomUUID().toString();
            Instant now = Instant.now();

            JdbcTemplate.create(
                    INSERT_CHANNEL_QUERY,
                    List.of(
                            channelId,
                            request.getName(),
                            request.getType().getNumber(),
                            request.getServerId(),
                            now.toString(),
                            now.toString(),
                            request.getProfileId()
                    )
            );

            JdbcUtils.doCommit();

            GrpcCreateChannelResponse response = GrpcCreateChannelResponse.newBuilder()
                    .setChannelId(channelId)
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

    @Override
    public void updateChannel(GrpcUpdateChannelRequest request, StreamObserver<GrpcUpdateChannelResponse> responseObserver) {
        // You can't specify target table 'tbl_channel' for update in FROM clause
        String IS_PERMISSION_QUERY = "select 1 from tbl_member where tbl_member.server_id = (select tbl_channel.server_id from tbl_channel where tbl_channel.id = ?) and tbl_member.profile_id = ? and tbl_member.role in (?, ?)";
        String UPDATE_CHANNEL_QUERY = "update tbl_channel set name = ? where id = ? and name <> ?";

        try {

            JdbcUtils.openSession(dataSource);

            boolean hasPermission = JdbcTemplate.exists(
                    IS_PERMISSION_QUERY,
                    List.of(
                            request.getChannelId(),
                            request.getProfileId(),
                            GrpcMemberRole.MEMBER_ROLE_ADMIN_VALUE,
                            GrpcMemberRole.MEMBER_ROLE_MODERATOR_VALUE
                    )
            );

            if (!hasPermission) {
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

            JdbcTemplate.update(
                    UPDATE_CHANNEL_QUERY,
                    List.of(
                            request.getName(),
                            request.getChannelId(),
                            "general"
                    )
            );

            JdbcUtils.doCommit();

            GrpcUpdateChannelResponse response = GrpcUpdateChannelResponse.newBuilder()
                    .setChannelId(request.getChannelId())
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

    @Override
    public void deleteChannel(GrpcDeleteChannelRequest request, StreamObserver<GrpcDeleteChannelResponse> responseObserver) {
        String IS_PERMISSION_QUERY = "select 1 from tbl_member where tbl_member.server_id = (select tbl_channel.server_id from tbl_channel where tbl_channel.id = ?) and tbl_member.profile_id = ? and tbl_member.role in (?, ?)";
        String DELETE_CHANNEL_QUERY = "delete from tbl_channel where tbl_channel.id = ? and tbl_channel.name <> ?";

        try {

            JdbcUtils.openSession(dataSource);

            boolean hasPermission = JdbcTemplate.exists(
                  IS_PERMISSION_QUERY,
                  List.of(
                          request.getChannelId(),
                          request.getProfileId(),
                          GrpcMemberRole.MEMBER_ROLE_ADMIN_VALUE,
                          GrpcMemberRole.MEMBER_ROLE_MODERATOR_VALUE
                  )
            );

            if (!hasPermission) {
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

            JdbcTemplate.delete(
                    DELETE_CHANNEL_QUERY,
                    List.of(
                            request.getChannelId(),
                            "general"
                    )
            );

            JdbcUtils.doCommit();

            GrpcDeleteChannelResponse response = GrpcDeleteChannelResponse.newBuilder()
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

    @Override
    public void getChannelGeneralByServerId(GrpcGetChannelGeneralByServerIdRequest request, StreamObserver<GrpcGetChannelGeneralByServerIdResponse> responseObserver) {
        String IS_MEMBER_QUERY = "exists (select 1 from tbl_member as m where m.profile_id = ? and m.server_id = ?)";
        String CHANNEL_QUERY = "select c.id, c.name, c.type, c.server_id, c.created_at, c.updated_at from tbl_channel as c " +
                "where c.server_id = ? and c.name = ? and " + IS_MEMBER_QUERY;

        try {

            JdbcUtils.initConnection(dataSource);

            GrpcChannel channel = JdbcTemplate.query(
                    CHANNEL_QUERY,
                    List.of(
                            request.getServerId(),
                            "general",
                            request.getProfileId(),
                            request.getServerId()
                    ),
                    rs -> {
                        while (rs.next()) {
                            return GrpcChannel.newBuilder()
                                    .setChannelId(rs.getString(1))
                                    .setName(rs.getString(2))
                                    .setType(GrpcChannelType.forNumber(rs.getInt(3)))
                                    .setServerId(rs.getString(4))
                                    .setCreatedAt(rs.getString(5))
                                    .setUpdatedAt(rs.getString(6))
                                    .build();
                        }

                        return null;
                    }
            );

            if (channel == null) {
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

            GrpcGetChannelGeneralByServerIdResponse response = GrpcGetChannelGeneralByServerIdResponse.newBuilder()
                    .setResult(channel)
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
    public void getChannelByChannelId(GrpcGetChannelByChannelIdRequest request, StreamObserver<GrpcGetChannelByChannelIdResponse> responseObserver) {
        String CHANNEL_QUERY = "select tbl_channel.id, tbl_channel.name, tbl_channel.type, tbl_channel.server_id, tbl_channel.created_at, tbl_channel.updated_at " +
                "from tbl_channel inner join tbl_member " +
                "on tbl_channel.server_id = tbl_member.server_id " +
                "where tbl_channel.id = ? and tbl_member.profile_id = ?";

        try {
            JdbcUtils.openSession(dataSource);

            GrpcChannel channel = JdbcTemplate.query(
                    CHANNEL_QUERY,
                    List.of(
                            request.getChannelId(),
                            request.getProfileId()
                    ),
                    rs -> {
                        while (rs.next()) {
                            return GrpcChannel.newBuilder()
                                    .setChannelId(rs.getString(1))
                                    .setName(rs.getString(2))
                                    .setType(GrpcChannelType.forNumber(rs.getInt(3)))
                                    .setServerId(rs.getString(4))
                                    .setCreatedAt(rs.getString(5))
                                    .setUpdatedAt(rs.getString(6))
                                    .build();
                        }
                        return null;
                    }
            );

            if (channel == null) {
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

            GrpcGetChannelByChannelIdResponse response = GrpcGetChannelByChannelIdResponse.newBuilder()
                    .setResult(channel)
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
    public void getChannelsByServerId(GrpcGetChannelsByServerIdRequest request, StreamObserver<GrpcGetChannelsByServerIdResponse> responseObserver) {
        String IS_MEMBER_QUERY = "exists (select 1 from tbl_member as m where m.profile_id = ? and m.server_id = ?)";

        String COUNT_QUERY = "select count(c.id) from tbl_channel as c where c.server_id = ? and " + IS_MEMBER_QUERY;
        String CHANNEL_QUERY = "select c.id, c.name, c.type, c.server_id, c.created_at, c.updated_at from tbl_channel as c " +
                "where c.server_id = ? and " + IS_MEMBER_QUERY + " limit ? offset ?";

        GrpcGetChannelsByServerIdResponse.Builder builder = GrpcGetChannelsByServerIdResponse.newBuilder();

        try {
            JdbcUtils.initConnection(dataSource);

            long totalElements = JdbcTemplate.count(
                    COUNT_QUERY,
                    List.of(
                            request.getServerId(),
                            request.getProfileId(),
                            request.getServerId()
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
                    CHANNEL_QUERY,
                    List.of(
                            request.getServerId(),
                            request.getProfileId(),
                            request.getServerId(),
                            request.getPageSize(),
                            request.getPageNumber() * request.getPageSize()
                    ),
                    rs -> {
                        while (rs.next()) {
                            builder.addContent(GrpcChannel.newBuilder()
                                    .setChannelId(rs.getString(1))
                                    .setName(rs.getString(2))
                                    .setType(GrpcChannelType.forNumber(rs.getInt(3)))
                                    .setServerId(rs.getString(4))
                                    .setCreatedAt(rs.getString(5))
                                    .setUpdatedAt(rs.getString(6))
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
}
