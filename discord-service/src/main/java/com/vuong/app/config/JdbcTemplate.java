package com.vuong.app.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcTemplate {

    public static void query(String sql, List<Object> params, ResultSetExtractor rse) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = JdbcUtils.getConnection();

        try {
            ps = con.prepareStatement(sql);

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            rs = ps.executeQuery();

            rse.extractData(rs);
        } finally {
            JdbcUtils.closePreparedStatement(ps);
            JdbcUtils.closeResultSet(rs);
        }
    }

    public static boolean exists(String sql, List<Object> params) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = JdbcUtils.getConnection();

        try {
            ps = con.prepareStatement(sql);

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            rs = ps.executeQuery();

            return JdbcUtils.hasResult(rs);
        } finally {
            JdbcUtils.closePreparedStatement(ps);
            JdbcUtils.closeResultSet(rs);
        }
    }

    public static long count(String sql, List<Object> params) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = JdbcUtils.getConnection();
        long count = 0;

        try {
            ps = con.prepareStatement(sql);

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            rs = ps.executeQuery();

            while (rs.next()) {
                count = rs.getLong(1);
            }

            return count;
        } finally {
            JdbcUtils.closePreparedStatement(ps);
            JdbcUtils.closeResultSet(rs);
        }
    }

    public static int create(String sql, List<Object> params) throws SQLException {
        return execute(sql, params);
    }

    public static int update(String sql, List<Object> params) throws SQLException {
        return execute(sql, params);
    }

    public static int delete(String sql, List<Object> params) throws SQLException {
        return execute(sql, params);
    }

    public static int execute(String sql, List<Object> params) throws SQLException {
        PreparedStatement ps = null;
        Connection con = JdbcUtils.getConnection();

        try {
            ps = con.prepareStatement(sql);

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            return ps.executeUpdate();
        } finally {
            JdbcUtils.closePreparedStatement(ps);
        }
    }

}
