package com.vuong.app.jdbc.sql;

public class ExistsBuilder {

    private SelectBuilder selectBuilder;

    public ExistsBuilder(SelectBuilder selectBuilder) {
        this.selectBuilder = selectBuilder;
    }

    public String toString() {
        return "exists (" + selectBuilder + ")";
    }

}
