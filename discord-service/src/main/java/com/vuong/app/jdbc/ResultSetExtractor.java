package com.vuong.app.jdbc;

import com.vuong.app.jdbc.exception.JdbcDataAccessException;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetExtractor<T> {
    T extractData(ResultSet rs) throws SQLException, JdbcDataAccessException;
}
