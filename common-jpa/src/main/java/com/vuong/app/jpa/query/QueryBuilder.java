package com.vuong.app.jpa.query;

import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class QueryBuilder {
    private List<Specification> querys;

    public QueryBuilder() {
        this.querys = new ArrayList<>();
    }

    public List<Specification> getParams() {
        return new ArrayList<>(this.querys);
    }

    public QueryBuilder query(Specification specification) {
        this.querys.add(specification);
        return this;
    }

    public Specification build() {
        if (querys.size() == 0) {
            return null;
        }

        Specification result = querys.get(0);

        for (int i = 1; i < querys.size(); i++) {
            result = Specification.where(result).and(querys.get(i));
        }

        return result;
    }
}
