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
public class ChannelService extends ChannelServiceGrpc.ChannelServiceImplBase {

    private final MysteryJdbc mysteryJdbc;

    @Override
    public void createChannel(GrpcCreateChannelRequest request, StreamObserver<GrpcCreateChannelResponse> responseObserver) {
        // server delete is member delete and channel delete
        String isPermissionQuery = "select 1 from tbl_member as m where m.server_id = ? and m.profile_id = ? and m.role in (?, ?)";
        String insertChannelQuery = "insert into tbl_channel(id, name, type, server_id, created_at, updated_at, updated_by) values(?, ?, ?, ?, ?, ?, ?)";
        // note: insert not working with where

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();

            pst1 = con.prepareStatement(isPermissionQuery);

            pst1.setString(1, request.getServerId());
            pst1.setString(2, request.getProfileId());
            pst1.setInt(3, GrpcMemberRole.MEMBER_ROLE_ADMIN_VALUE);
            pst1.setInt(4, GrpcMemberRole.MEMBER_ROLE_MODERATOR_VALUE);
            rs = pst1.executeQuery();

            boolean hasPermistion = rs.next();

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
            String createdAt = Instant.now().toString();
            String updatedAt = createdAt;

            pst2 = con.prepareStatement(insertChannelQuery);

            pst2.setString(1, channelId);
            pst2.setString(2, request.getName());
            pst2.setInt(3, request.getType().getNumber());
            pst2.setString(4, request.getServerId());
            pst2.setString(5, createdAt);
            pst2.setString(6, updatedAt);
            pst2.setString(7, request.getProfileId());

            int result = pst2.executeUpdate();

            GrpcCreateChannelResponse response = GrpcCreateChannelResponse.newBuilder()
                    .setChannelId(channelId)
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

    @Override
    public void updateChannel(GrpcUpdateChannelRequest request, StreamObserver<GrpcUpdateChannelResponse> responseObserver) {
        String isPermissionQuery = "exists (select 1 from tbl_member as m where m.server_id = (select tbl_channel.server_id from tbl_channel where tbl_channel.id = ?) and m.profile_id = ? and m.role in (?, ?))";
        String updateChannelQuery = "update tbl_channel set name = ? where id = ? and name <> ? and " + isPermissionQuery;

        Connection con = null;
        PreparedStatement pst = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(updateChannelQuery);
            pst.setString(1, request.getName());
            pst.setString(2, request.getChannelId());
            pst.setString(3, "general");
            pst.setString(4, request.getChannelId());
            pst.setString(5, request.getProfileId());
            pst.setInt(6, GrpcMemberRole.MEMBER_ROLE_ADMIN_VALUE);
            pst.setInt(7, GrpcMemberRole.MEMBER_ROLE_MODERATOR_VALUE);

            int result = pst.executeUpdate();

            GrpcUpdateChannelResponse response = GrpcUpdateChannelResponse.newBuilder()
                    .setChannelId(request.getChannelId())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    @Override
    public void deleteChannel(GrpcDeleteChannelRequest request, StreamObserver<GrpcDeleteChannelResponse> responseObserver) {
        String isPermissionQuery = "exists (select 1 from tbl_member as m where m.server_id = (select tbl_channel.server_id from tbl_channel where tbl_channel.id = ?) and m.profile_id = ? and m.role in (?, ?))";
        String deleteChannelQuery = "delete tbl_channel where id = ? and name <> ? and " + isPermissionQuery;

        Connection con = null;
        PreparedStatement pst = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(deleteChannelQuery);
            pst.setString(1, request.getChannelId());
            pst.setString(2, "general");
            pst.setString(3, request.getChannelId());
            pst.setString(4, request.getProfileId());
            pst.setInt(5, GrpcMemberRole.MEMBER_ROLE_ADMIN_VALUE);
            pst.setInt(6, GrpcMemberRole.MEMBER_ROLE_MODERATOR_VALUE);

            int result = pst.executeUpdate();

            GrpcDeleteChannelResponse response = GrpcDeleteChannelResponse.newBuilder()
                    .setDeleted(true)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException ex) {
            ex.printStackTrace();
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    @Override
    public void getChannelGeneralByServerId(GrpcGetChannelGeneralByServerIdRequest request, StreamObserver<GrpcGetChannelGeneralByServerIdResponse> responseObserver) {
        String isMemberQuery = "exists (select 1 from tbl_member as m where m.profile_id = ? and m.server_id = ?)";
        String serverQuery = "select c.id, c.name, c.type, c.server_id, c.created_at, c.updated_at from tbl_channel as c " +
                "where c.server_id = ? and c.name = ? and " + isMemberQuery;

        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(serverQuery);

            pst.setString(1, request.getServerId());
            pst.setString(2, "general");

            pst.setString(3, request.getProfileId());
            pst.setString(4, request.getServerId());

            rs = pst.executeQuery();

            GrpcChannel channel = null;

            while (rs.next()) {
                channel = GrpcChannel.newBuilder()
                        .setChannelId(rs.getString(1))
                        .setName(rs.getString(2))
                        .setType(GrpcChannelType.forNumber(rs.getInt(3)))
                        .setServerId(rs.getString(4))
                        .setCreatedAt(rs.getString(5))
                        .setUpdatedAt(rs.getString(6))
                        .build();
            }

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
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    @Override
    public void getChannelByChannelId(GrpcGetChannelByChannelIdRequest request, StreamObserver<GrpcGetChannelByChannelIdResponse> responseObserver) {
        String channelQuery = "select tbl_channel.id, tbl_channel.name, tbl_channel.type, tbl_channel.server_id, tbl_channel.created_at, tbl_channel.updated_at " +
                "from tbl_channel inner join tbl_member " +
                "on tbl_channel.server_id = tbl_member.server_id " +
                "where tbl_channel.id = ? and tbl_member.profile_id = ?";

        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(channelQuery);

            pst.setString(1, request.getChannelId());
            pst.setString(2, request.getProfileId());

            rs = pst.executeQuery();

            GrpcChannel channel = null;

            while (rs.next()) {
                channel = GrpcChannel.newBuilder()
                        .setChannelId(rs.getString(1))
                        .setName(rs.getString(2))
                        .setType(GrpcChannelType.forNumber(rs.getInt(3)))
                        .setServerId(rs.getString(4))
                        .setCreatedAt(rs.getString(5))
                        .setUpdatedAt(rs.getString(6))
                        .build();
            }

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
            mysteryJdbc.closeResultSet(rs);
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    @Override
    public void getChannelsByServerId(GrpcGetChannelsByServerIdRequest request, StreamObserver<GrpcGetChannelsByServerIdResponse> responseObserver) {
        String isMemberQuery = "exists (select 1 from tbl_member as m where m.profile_id = ? and m.server_id = ?)";

        String countQuery = "select count(c.id) from tbl_channel as c where c.server_id = ? and " + isMemberQuery;
        String channelQuery = "select c.id, c.name, c.type, c.server_id, c.created_at, c.updated_at from tbl_channel as c " +
                "where c.server_id = ? and " + isMemberQuery + " limit ? offset ?";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        GrpcGetChannelsByServerIdResponse.Builder builder = GrpcGetChannelsByServerIdResponse.newBuilder();

        try {
            con = mysteryJdbc.getConnection();

            pst1 = con.prepareStatement(countQuery);

            pst1.setString(1, request.getServerId());

            pst1.setString(2, request.getProfileId());
            pst1.setString(3, request.getServerId());
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

            pst2 = con.prepareStatement(channelQuery);
            pst2.setString(1, request.getServerId());

            pst2.setString(2, request.getProfileId());
            pst2.setString(3, request.getServerId());

            pst2.setInt(4, request.getPageSize());
            pst2.setInt(5, request.getPageNumber() * request.getPageSize());

            rs2 = pst2.executeQuery();
            while (rs2.next()) {
                builder.addContent(GrpcChannel.newBuilder()
                        .setChannelId(rs2.getString(1))
                        .setName(rs2.getString(2))
                        .setType(GrpcChannelType.forNumber(rs2.getInt(3)))
                        .setServerId(rs2.getString(4))
                        .setCreatedAt(rs2.getString(5))
                        .setUpdatedAt(rs2.getString(6))
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
}
