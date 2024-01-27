package com.vuong.app.jdbc;

import com.vuong.app.jdbc.exception.JdbcDataAccessException;

import java.sql.*;

public class JdbcClient {

    public static JdbcClientBuilder sql(String sql) {
        return new JdbcClientBuilder(sql);
    }

    public static class JdbcClientBuilder {
        private PreparedStatement pst;

        public JdbcClientBuilder(String sql) throws JdbcDataAccessException {
            Connection con = SqlSession.getConnection();
            try {
                this.pst = con.prepareStatement(sql);
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            }
        }

        public JdbcClientBuilder params(PreparedStatementCallback action) throws JdbcDataAccessException {
            try {
                action.doInStatement(this.pst);
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            }
            return this;
        }

        public <T> T query(ResultSetExtractor<T> rse) throws JdbcDataAccessException {
            ResultSet rs = null;
            try {
                rs = this.pst.executeQuery();
                return rse.extractData(rs);
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            } finally {
                JdbcUtils.closePreparedStatement(this.pst);
                JdbcUtils.closeResultSet(rs);
            }
        }

        public boolean exists() throws JdbcDataAccessException {
            ResultSet rs = null;
            try {
                rs = this.pst.executeQuery();
                return rs.isBeforeFirst();
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            } finally {
                JdbcUtils.closePreparedStatement(this.pst);
                JdbcUtils.closeResultSet(rs);
            }
        }

        public long count() throws JdbcDataAccessException {
            ResultSet rs = null;
            try {
                rs = this.pst.executeQuery();
                while (rs.next()) {
                    return rs.getLong(1);
                }
                return 0;
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            } finally {
                JdbcUtils.closePreparedStatement(this.pst);
                JdbcUtils.closeResultSet(rs);
            }
        }

        private int execute() throws JdbcDataAccessException {
            try {
                return this.pst.executeUpdate();
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            } finally {
                JdbcUtils.closePreparedStatement(this.pst);
            }
        }

        public int insert() throws JdbcDataAccessException {
            return this.execute();
        }

        public int update() throws JdbcDataAccessException {
            return this.execute();
        }

        public int delete() throws JdbcDataAccessException {
            return this.execute();
        }
    }
}
