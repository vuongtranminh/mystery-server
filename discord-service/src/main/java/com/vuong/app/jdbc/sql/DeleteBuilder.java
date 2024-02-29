package com.vuong.app.jdbc.sql;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class DeleteBuilder extends AbstractSqlBuilder implements Serializable {

    private static final long serialVersionUID = 1;

    private String table;

    private List<String> wheres = new ArrayList<String>();

    public DeleteBuilder(String table) {
        this.table = table;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder("delete from ").append(table);
        appendList(sql, wheres, " where ", " and ");
        log.info(sql.toString());
        return sql.toString();
    }

    public DeleteBuilder where(String expr) {
        wheres.add(expr);
        return this;
    }

}

