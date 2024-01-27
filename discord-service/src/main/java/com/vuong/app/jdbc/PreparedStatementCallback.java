package com.vuong.app.jdbc;

import com.vuong.app.jdbc.exception.JdbcDataAccessException;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PreparedStatementCallback {
    void doInStatement(PreparedStatement pst) throws SQLException, JdbcDataAccessException;
}
