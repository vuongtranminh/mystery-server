package com.vuong.app.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface MysteryJdbc {

    Connection getConnection();

    void closeConnection();

    void closePreparedStatement(PreparedStatement... psts);

    void closeResultSet(ResultSet... rss);

    void doRollback();

    void doCommit();

    void setAutoCommit(boolean autoCommit);
}
