package com.vuong.app.jdbc;

import com.vuong.app.jdbc.exception.JdbcDataAccessException;

import java.math.BigDecimal;
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

        public JdbcClientBuilder setBoolean(int parameterIndex, boolean x) throws JdbcDataAccessException {
            try {
                this.pst.setBoolean(parameterIndex, x);
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            }
            return this;
        }

        public JdbcClientBuilder setByte(int parameterIndex, byte x) throws JdbcDataAccessException {
            try {
                this.pst.setByte(parameterIndex, x);
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            }
            return this;
        }

        public JdbcClientBuilder setShort(int parameterIndex, short x) throws JdbcDataAccessException {
            try {
                this.pst.setShort(parameterIndex, x);
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            }
            return this;
        }

        public JdbcClientBuilder setInt(int parameterIndex, int x) throws JdbcDataAccessException {
            try {
                this.pst.setInt(parameterIndex, x);
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            }
            return this;
        }

        public JdbcClientBuilder setLong(int parameterIndex, long x) throws JdbcDataAccessException {
            try {
                this.pst.setLong(parameterIndex, x);
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            }
            return this;
        }

        public JdbcClientBuilder setFloat(int parameterIndex, float x) throws JdbcDataAccessException {
            try {
                this.pst.setFloat(parameterIndex, x);
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            }
            return this;
        }

        public JdbcClientBuilder setDouble(int parameterIndex, double x) throws JdbcDataAccessException {
            try {
                this.pst.setDouble(parameterIndex, x);
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            }
            return this;
        }

        public JdbcClientBuilder setBigDecimal(int parameterIndex, BigDecimal x) throws JdbcDataAccessException {
            try {
                this.pst.setBigDecimal(parameterIndex, x);
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            }
            return this;
        }

        public JdbcClientBuilder setString(int parameterIndex, String x) throws JdbcDataAccessException {
            try {
                this.pst.setString(parameterIndex, x);
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            }
            return this;
        }

        public JdbcClientBuilder setBytes(int parameterIndex, byte x[]) throws JdbcDataAccessException {
            try {
                this.pst.setBytes(parameterIndex, x);
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            }
            return this;
        }

        public JdbcClientBuilder setDate(int parameterIndex, java.sql.Date x) throws JdbcDataAccessException {
            try {
                this.pst.setDate(parameterIndex, x);
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            }
            return this;
        }

        public JdbcClientBuilder setTime(int parameterIndex, java.sql.Time x) throws JdbcDataAccessException {
            try {
                this.pst.setTime(parameterIndex, x);
            } catch (SQLException e) {
                throw new JdbcDataAccessException(e);
            }
            return this;
        }

        public JdbcClientBuilder setTimestamp(int parameterIndex, java.sql.Timestamp x) throws JdbcDataAccessException {
            try {
                this.pst.setTimestamp(parameterIndex, x);
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
