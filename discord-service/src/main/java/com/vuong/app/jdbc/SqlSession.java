package com.vuong.app.jdbc;

import com.vuong.app.jdbc.exception.JdbcDataAccessException;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class SqlSession {
    private final DataSource dataSource;
    private static ThreadLocal<Connection> localConnection = new ThreadLocal<>();

    public SqlSession(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static Connection getConnection() {
        return localConnection.get();
    }

    private Connection fetchConnection() throws SQLException {
        Connection con = this.dataSource.getConnection();
        if (con == null) {
            throw new IllegalStateException("DataSource returned null from getConnection(): " + dataSource);
        }
        return con;
    }

    public Connection openSession() throws JdbcDataAccessException {
        return openSession(false);
    }

    public Connection openSession(boolean autoCommit) {
        try {
            Connection con = this.fetchConnection();
            con.setAutoCommit(autoCommit);
            localConnection.set(con);
            return con;
        } catch (SQLException e) {
            throw new JdbcDataAccessException(e);
        }
    }

    public void closeConnection() {
        Connection con = localConnection.get();
        if (con == null) {
            throw new JdbcDataAccessException("Error:  Cannot close.  No managed session is started.");
        }

        try {
            con.close();
        }
        catch (SQLException ex) {
            log.debug("Could not close JDBC Connection", ex);
        }
        catch (Throwable ex) {
            // We don't trust the JDBC driver: It might throw RuntimeException or Error.
            log.debug("Unexpected exception on closing JDBC Connection", ex);
        } finally {
            localConnection.remove();
        }
    }

    public void rollback() {
        Connection con = localConnection.get();
        if (con == null) {
            throw new JdbcDataAccessException("Error:  Cannot close.  No managed session is started.");
        }

        try {
            con.rollback();
        } catch (SQLException ex) {
            log.trace("JDBC rollback", ex);
        }
    }

    public void commit() {
        Connection con = localConnection.get();

        if (con == null) {
            throw new JdbcDataAccessException("Error:  Cannot close.  No managed session is started.");
        }

        try {
            con.commit();
        } catch (SQLException ex) {
            log.trace("JDBC commit", ex);
        }
    }
}
