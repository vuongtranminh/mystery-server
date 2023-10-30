package com.vuong.app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@RequiredArgsConstructor
public class MysteryJdbcImpl implements MysteryJdbc {

    private final Connection con;

    @Override
    public Connection getConnection() {
        return this.con;
    }

    @Override
    public void closeConnection() {

    }

    @Override
    public void closePreparedStatement(PreparedStatement... psts) {
        for (PreparedStatement pst : psts) {
            this.closePreparedStatement(pst);
        }
    }

    @Override
    public void closeResultSet(ResultSet... rss) {
        for (ResultSet rs : rss) {
            this.closeResultSet(rs);
        }
    }

    @Override
    public void doRollback() {
        try {
            this.con.rollback();
        } catch (SQLException ex) {
            log.trace("JDBC rollback", ex);
        }
    }

    @Override
    public void doCommit() {
        try {
            this.con.commit();
        } catch (SQLException ex) {
            log.trace("JDBC commit", ex);
        }
    }

    @Override
    public void setAutoCommit(boolean autoCommit) {
        try {
            this.con.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void closePreparedStatement(PreparedStatement pst) {
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

    private void closeResultSet(ResultSet rs) {
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
