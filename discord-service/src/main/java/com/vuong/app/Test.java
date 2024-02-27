package com.vuong.app;

import com.vuong.app.jdbc.sql.SelectBuilder;

public class Test {
    public static void main(String[] args) {
        String MESSAGES_QUERY1 = "select " +
                "            tbl_message.id, tbl_message.channel_id, tbl_message.created_at, tbl_message.updated_at, " +
                "            tbl_channel.server_id, " +
                "            tbl_profile.id, tbl_profile.name, tbl_profile.avt_url " +
                "            from tbl_message " +
                "            inner join tbl_channel " +
                "            on tbl_message.channel_id = tbl_channel.id " +
                "            inner join tbl_profile " +
                "            on tbl_message.created_by = tbl_profile.id " +
                "            where tbl_message.id = ? " +
                "            and tbl_message.deleted_at is null";
//
//        String MEMBER_OWNER_MESSAGE_QUERY = "select " +
//                "            tbl_member.id, tbl_member.role, tbl_member.server_id, tbl_member.join_at " +
//                "            from tbl_member " +
//                "            where tbl_member.profile_id = ? " +
//                "            and tbl_member.server_id = ?";
//
//        String UPDATE_MESAGE_QUERY = "update tbl_message set content = ?, updated_at = ? where tbl_message.id = ?";

        String MESSAGES_QUERY = new SelectBuilder()
                .column("tbl_message.id")
                .column("tbl_message.channel_id")
                .column("tbl_message.created_at")
                .column("tbl_message.updated_at")
                .column("tbl_channel.server_id")
                .column("tbl_profile.id")
                .column("tbl_profile.name")
                .column("tbl_profile.avt_url")
                .from("tbl_message")
                .join("tbl_channel on tbl_message.channel_id = tbl_channel.id")
                .join("tbl_profile on tbl_message.created_by = tbl_profile.id")
                .where("tbl_message.id = ?")
                .where("tbl_message.deleted_at is null")
                .toString();

        System.out.println(MESSAGES_QUERY);

        System.out.println(MESSAGES_QUERY1.equals(MESSAGES_QUERY));
    }
}
