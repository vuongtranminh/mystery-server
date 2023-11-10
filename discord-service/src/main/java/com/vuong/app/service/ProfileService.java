package com.vuong.app.service;

import com.vuong.app.config.MysteryJdbc;
import com.vuong.app.model.CreateProfileRequest;
import com.vuong.app.model.UpdateProfileRequest;
import com.vuong.app.v1.GrpcErrorCode;
import com.vuong.app.v1.GrpcErrorResponse;
import com.vuong.app.v1.discord.GrpcCreateChannelResponse;
import com.vuong.app.v1.discord.GrpcMemberRole;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final MysteryJdbc mysteryJdbc;

    public void createProfile(CreateProfileRequest request) {
        String insertProfileQuery = "insert into tbl_profile(id, name, avt_url) values (?, ?, ?)";
        // note: insert not working with where

        Connection con = null;
        PreparedStatement pst = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(insertProfileQuery);

            pst.setString(1, request.getProfileId());
            pst.setString(2, request.getName());
            pst.setString(3, request.getAvtUrl());
            int result = pst.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closePreparedStatement(pst);
        }
    }

    public void updateProfile(UpdateProfileRequest request) {
        String updateProfileQuery = "update tbl_profile set name = ?, avt_url = ? where tbl_profile.id = ?";
        // note: insert not working with where

        Connection con = null;
        PreparedStatement pst = null;

        try {
            con = mysteryJdbc.getConnection();

            pst = con.prepareStatement(updateProfileQuery);

            pst.setString(1, request.getName());
            pst.setString(2, request.getAvtUrl());
            pst.setString(3, request.getProfileId());
            int result = pst.executeUpdate();
        } catch (SQLException ex) {
            mysteryJdbc.doRollback();
        } finally {
            mysteryJdbc.closePreparedStatement(pst);
        }
    }
}
