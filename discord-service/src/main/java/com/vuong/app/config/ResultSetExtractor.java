package com.vuong.app.config;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetExtractor {
    void extractData(ResultSet rs) throws SQLException;
}
