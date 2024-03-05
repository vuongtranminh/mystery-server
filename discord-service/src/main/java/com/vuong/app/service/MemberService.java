package com.vuong.app.service;

import com.vuong.app.jdbc.JdbcClient;
import com.vuong.app.jdbc.JdbcUtils;
import com.vuong.app.jdbc.SqlSession;
import com.vuong.app.jdbc.exception.JdbcDataAccessException;
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

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class MemberService extends MemberServiceGrpc.MemberServiceImplBase {

    private final SqlSession sqlSession;

    @Override
    public void getMembersByServerId(GrpcGetMembersByServerIdRequest request, StreamObserver<GrpcGetMembersByServerIdResponse> responseObserver) {
        String IS_MEMBER_QUERY = "exists (select 1 from tbl_member as m1 where m1.profile_id = ? and m1.server_id = ?)";
        String COUNT_QUERY = "select count(m.id) from tbl_member as m where m.server_id = ? and m.profile_id <> ? and " + IS_MEMBER_QUERY;

        String MEMBER_PROFILE_QUERY = "select " +
                "ms.id as member_id, p.id as profile_id, ms.role as member_role, " +
                "ms.join_at as member_join_at, p.name as profile_name, p.avt_url as profile_avt_url " +
                "from tbl_profile as p inner join " +
                "(select * from tbl_member as m where m.server_id = ? and m.profile_id <> ? and " + IS_MEMBER_QUERY +
                " order by m.role asc limit ? offset ?) as ms " +
                "on p.id = ms.profile_id";

        GrpcGetMembersByServerIdResponse.Builder builder = GrpcGetMembersByServerIdResponse.newBuilder();

        try {
            sqlSession.openSession();

            long totalElements = JdbcClient.sql(COUNT_QUERY)
                    .setString(1, request.getServerId())
                    .setString(2, request.getProfileId())
                    .setString(3, request.getProfileId())
                    .setString(4, request.getServerId())
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

            JdbcClient.sql(MEMBER_PROFILE_QUERY)
                    .setString(1, request.getServerId())
                    .setString(2, request.getProfileId())
                    .setString(3, request.getProfileId())
                    .setString(4, request.getServerId())
                    .setInt(5, request.getPageSize())
                    .setInt(6, request.getPageNumber() * request.getPageSize())
                    .query(rs -> {
                        while (rs.next()) {
                            builder.addContent(GrpcMemberProfile.newBuilder()
                                    .setMemberId(rs.getString(1))
                                    .setProfileId(rs.getString(2))
                                    .setRole(GrpcMemberRole.forNumber(rs.getInt(3)))
                                    .setJoinAt(rs.getString(4))
                                    .setName(rs.getString(5))
                                    .setAvtUrl(rs.getString(6))
                                    .build());
                        }
                        return null;
                    });

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (JdbcDataAccessException ex) {
            ex.printStackTrace();
        } finally {
            sqlSession.closeConnection();
        }
    }

    @Override
    public void getMemberByServerId(GrpcGetMemberByServerIdRequest request, StreamObserver<GrpcGetMemberByServerIdResponse> responseObserver) {
        String MEMBER_QUERY = "select * from tbl_member as m where m.profile_id = ? and m.server_id = ?";
        String MEMBER_PROFILE_QUERY = "select " +
                "ms.id as member_id, p.id as profile_id, ms.role as member_role, " +
                "ms.join_at as member_join_at, p.name as profile_name, p.avt_url as profile_avt_url " +
                "from tbl_profile as p inner join " +
                "(" + MEMBER_QUERY + ") as ms " +
                "on p.id = ms.profile_id";

        try {
            sqlSession.openSession();

            GrpcMemberProfile memberProfile = JdbcClient.sql(MEMBER_PROFILE_QUERY)
                    .setString(1, request.getProfileId())
                    .setString(2, request.getServerId())
                    .query(rs -> {
                        while (rs.next()) {
                            return GrpcMemberProfile.newBuilder()
                                    .setMemberId(rs.getString(1))
                                    .setProfileId(rs.getString(2))
                                    .setRole(GrpcMemberRole.forNumber(rs.getInt(3)))
                                    .setJoinAt(rs.getString(4))
                                    .setName(rs.getString(5))
                                    .setAvtUrl(rs.getString(6))
                                    .build();
                        }
                        return null;
                    });

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
        } catch (JdbcDataAccessException ex) {
            ex.printStackTrace();
        } finally {
            sqlSession.closeConnection();
        }
    }

    // member_role = tbl_server_role.id
    // tbl_server_role (id, name, server_id, created_at, updated_at, permistion_point) // permistion point check like linux
    // tbl_server_permistion (hệ thống cài đặt)
}
