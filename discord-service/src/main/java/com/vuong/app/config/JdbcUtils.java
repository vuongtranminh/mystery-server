package com.vuong.app.config;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;

@Slf4j
public class JdbcUtils {

    private static ThreadLocal<Connection> localConnection = new ThreadLocal<>();

    public static Connection getConnection() {
        return localConnection.get();
    }

    public static Connection initConnection(DataSource dataSource) {
        try {
            Connection con = dataSource.getConnection();
            localConnection.set(con);
            return con;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Connection openSession(DataSource dataSource) {
        Connection con = initConnection(dataSource);
        setAutoCommit(false);
        return con;
    }

    public static void closeConnection() {
        Connection con = localConnection.get();
        if (con != null) {
            try {
                con.close();
                localConnection.remove();
            }
            catch (SQLException ex) {
                log.debug("Could not close JDBC Connection", ex);
            }
            catch (Throwable ex) {
                // We don't trust the JDBC driver: It might throw RuntimeException or Error.
                log.debug("Unexpected exception on closing JDBC Connection", ex);
            }
        }
    }

    public static void closePreparedStatement(PreparedStatement... psts) {
        for (PreparedStatement pst : psts) {
            closePreparedStatement(pst);
        }
    }

    public static void closeResultSet(ResultSet... rss) {
        for (ResultSet rs : rss) {
            closeResultSet(rs);
        }
    }

    public static void doRollback() {
        Connection con = localConnection.get();
        try {
            con.rollback();
        } catch (SQLException ex) {
            log.trace("JDBC rollback", ex);
        }
    }

    public static void doCommit() {
        Connection con = localConnection.get();
        try {
            con.commit();
        } catch (SQLException ex) {
            log.trace("JDBC commit", ex);
        }
    }

    public static void setAutoCommit(boolean autoCommit) {
        Connection con = localConnection.get();
        try {
            con.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasResult(ResultSet rs) throws SQLException {
        if (!rs.isBeforeFirst() ) {
            return false;
        }
        return true;
    }

    public static void closePreparedStatement(PreparedStatement pst) {
        if (pst != null) {
            try {
                pst.close();
            } catch (SQLException ex) {
                log.trace("Could not close JDBC Statement", ex);
            } catch (Throwable ex) {
                // We don't trust the JDBC driver: It might throw RuntimeException or Error.
                log.trace("Unexpected exception on closing JDBC Statement", ex);
            }
        }
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                log.trace("Could not close JDBC ResultSet", ex);
            } catch (Throwable ex) {
                // We don't trust the JDBC driver: It might throw RuntimeException or Error.
                log.trace("Unexpected exception on closing JDBC ResultSet", ex);
            }
        }
    }
}
