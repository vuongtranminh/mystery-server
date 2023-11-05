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
import net.devh.boot.grpc.server.service.GrpcService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

@GrpcService
@RequiredArgsConstructor
public class MemberService extends MemberServiceGrpc.MemberServiceImplBase {

    private final MysteryJdbc mysteryJdbc;

    @Override
    public void getMembersByServerId(GrpcGetMembersByServerIdRequest request, StreamObserver<GrpcGetMembersByServerIdResponse> responseObserver) {
        String isMemberQuery = "exists (select 1 from tbl_member as m1 where m1.profile_id = ? and m1.server_id = ?)";
        String countQuery = "select count(m.id) from tbl_member as m where m.server_id = ? and " + isMemberQuery;

        String memberProfileQuery = "select " +
                "ms.id as member_id, p.id as profile_id, ms.role as member_role, " +
                "ms.join_at as member_join_at, p.name as profile_name, p.avt_url as profile_avt_url " +
                "from tbl_profile as p inner join " +
                "(select * from tbl_member as m where m.server_id = ? and m.profile_id <> ? and " + isMemberQuery +
                " order by m.role asc limit ? offset ?) as ms " +
                "on p.id = ms.profile_id";

        Connection con = null;
        PreparedStatement pst1 = null;
        PreparedStatement pst2 = null;
        ResultSet rs1 = null;
        ResultSet rs2 = null;

        GrpcGetMembersByServerIdResponse.Builder builder = GrpcGetMembersByServerIdResponse.newBuilder();

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

            pst2 = con.prepareStatement(memberProfileQuery);
            pst2.setString(1, request.getServerId());
            pst2.setString(2, request.getProfileId());
            pst2.setString(3, request.getProfileId());
            pst2.setString(4, request.getServerId());
            pst2.setInt(5, request.getPageSize());
            pst2.setInt(6, request.getPageNumber() * request.getPageSize());

            rs2 = pst2.executeQuery();

            while (rs2.next()) {
                builder.addContent(GrpcMemberProfile.newBuilder()
                        .setMemberId(rs2.getString(1))
                        .setProfileId(rs2.getString(2))
                        .setRole(GrpcMemberRole.forNumber(rs2.getInt(3)))
                        .setJoinAt(rs2.getString(4))
                        .setName(rs2.getString(5))
                        .setAvtUrl(rs2.getString(6))
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
    public void getMemberByServerId(GrpcGetMemberByServerIdRequest request, StreamObserver<GrpcGetMemberByServerIdResponse> responseObserver) {
        String memberQuery = "select * from tbl_member as m where m.profile_id = ? and m.server_id = ?";
        String memberProfileQuery = "select " +
                "ms.id as member_id, p.id as profile_id, ms.role as member_role, " +
                "ms.join_at as member_join_at, p.name as profile_name, p.avt_url as profile_avt_url " +
                "from tbl_profile as p inner join " +
                "(" + memberQuery + ") as ms " +
                "on p.id = ms.profile_id";

        Connection con = null;
        PreparedStatement pst = null;
        ResultSet rs = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(memberProfileQuery);
            pst.setString(1, request.getProfileId());
            pst.setString(2, request.getServerId());

            rs = pst.executeQuery();

            GrpcMemberProfile memberProfile = null;

            while (rs.next()) {
                memberProfile = GrpcMemberProfile.newBuilder()
                        .setMemberId(rs.getString(1))
                        .setProfileId(rs.getString(2))
                        .setRole(GrpcMemberRole.forNumber(rs.getInt(3)))
                        .setJoinAt(rs.getString(4))
                        .setName(rs.getString(5))
                        .setAvtUrl(rs.getString(6))
                        .build();
            }

            if (memberProfile == null) {
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

            GrpcGetMemberByServerIdResponse response = GrpcGetMemberByServerIdResponse.newBuilder()
                    .setResult(memberProfile)
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
}
