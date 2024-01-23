package com.vuong.app.config;

import java.sql.SQLException;
import java.util.List;

public class JdbcClient {

    public static JdbcClientBuilder sql(String sql) {
        return new JdbcClientBuilder(sql);
    }

    public static class JdbcClientBuilder {
        private String sql;
        private List<Object> params;

        JdbcClientBuilder(String sql) {
            this.sql = sql;
        }

        public JdbcClientBuilder params(Object... args) {
            this.params = List.of(args);
            return this;
        }

        public <T> T query(ResultSetExtractor<T> rse) throws SQLException {
            return JdbcTemplate.query(this.sql, this.params, rse);
        }

        public int create() throws SQLException {
            return JdbcTemplate.update(this.sql, this.params);
        }

        public int update() throws SQLException {
            return JdbcTemplate.update(this.sql, this.params);
        }

        public int delete() throws SQLException {
            return JdbcTemplate.update(this.sql, this.params);
        }

        public boolean exists() throws SQLException {
            return JdbcTemplate.exists(this.sql, this.params);
        }

        public long count() throws SQLException {
            return JdbcTemplate.count(this.sql, this.params);
        }

        public String toString() {
            return "JdbcClient.JdbcClientBuilder(sql=" + this.sql + ", params=" + this.params + ")";
        }
    }
}
