package com.vuong.app.jpa.query;

import com.vuong.app.operator.*;
import com.vuong.app.v1.operator.OperatorProto;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class QueryHelper {

    public static void buildOneListOperatorFilter(QueryBuilder queryBuilder, ListOperators listOperators,
                                                  String fieldName) {
        if (listOperators == null) return;
        if (!CollectionUtils.isEmpty(listOperators.getIn())) {
            queryBuilder.query((root, query, criteriaBuilder) -> root.get(fieldName).in(listOperators.getIn()));
        } else if (!CollectionUtils.isEmpty(listOperators.getNotIn())) {
            queryBuilder.query((root, query, criteriaBuilder) -> root.get(fieldName).in(listOperators.getIn()).not());
        }

    }

    public static void buildOneStringOperatorFilter(QueryBuilder queryBuilder, StringOperators stringOperators,
                                                    String fieldName) {
        if (stringOperators == null) return;
        if (!StringUtils.isEmpty(stringOperators.getEq())) {
            queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.equal(criteriaBuilder.upper(root.get(fieldName)), stringOperators.getEq().toUpperCase()));
        } else if (!StringUtils.isEmpty(stringOperators.getContains())) {
            queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.like(
                    criteriaBuilder.upper(root.get(fieldName)),
                    "%" + stringOperators.getContains().toUpperCase() + "%"));
        } else if (!StringUtils.isEmpty(stringOperators.getNotEq())) {
            queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.notEqual(criteriaBuilder.upper(root.get(fieldName)), stringOperators.getEq().toUpperCase()));
        } else if (!StringUtils.isEmpty(stringOperators.getNotContains())) {
            queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.notLike(
                    criteriaBuilder.upper(root.get(fieldName)),
                    "%" + stringOperators.getContains().toUpperCase() + "%"));
        }
    }

    public static void buildOneDateOperatorFilter(QueryBuilder queryBuilder, DateOperators dateOperators,
                                                  String fieldName) {
        if (dateOperators == null) return;
        if (dateOperators.getEq() != null) {
            queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(fieldName), dateOperators.getEq()));
        } else if (dateOperators.getBefore() != null) {
            queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get(fieldName), dateOperators.getBefore()));
        } else if (dateOperators.getAfter() != null) {
            queryBuilder.query(((root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get(fieldName), dateOperators.getAfter())));
        } else if (dateOperators.getBetween() != null) {
            DateRange dateRange = dateOperators.getBetween();
            if (dateRange.getStart() != null && dateRange.getEnd() != null) {
                queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.between(root.get(fieldName), dateRange.getStart(), dateRange.getEnd()));
            }
        }
    }

    public static void buildOneBooleanOperatorFilter(QueryBuilder queryBuilder, BooleanOperators booleanOperators,
                                                     String fieldName) {
        if (booleanOperators == null) return;
        if (booleanOperators.getEq() != null) {
            queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(fieldName), booleanOperators.getEq()));
        }
    }

    public static void buildOneNumberOperatorFilter(QueryBuilder queryBuilder, NumberOperators numberOperators,
                                                    String fieldName) {
        if (numberOperators == null) return;
        if (numberOperators.getEq() != null) {
            queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(fieldName), numberOperators.getEq()));
        } else if (numberOperators.getNotEq() != null) {
            queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get(fieldName), numberOperators.getGt()));
        } else if (numberOperators.getGt() != null) {
            queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.gt(root.get(fieldName), numberOperators.getGt()));
        } else if (numberOperators.getGte() != null) {
            queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.ge(root.get(fieldName), numberOperators.getGte()));
        } else if (numberOperators.getLt() != null) {
            queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.lt(root.get(fieldName), numberOperators.getLt()));
        } else if (numberOperators.getLte() != null) {
            queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.le(root.get(fieldName), numberOperators.getLte()));
        } else if (numberOperators.getBetween() != null) {
            NumberRange numberRange = numberOperators.getBetween();
            if(numberRange.getStart() != null && numberRange.getEnd() != null) {
                queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.between(root.get(fieldName), numberRange.getStart(), numberRange.getEnd()));
            }
        }

    }

    public static void buildOneSortOrder(QueryBuilder queryBuilder, SortOrder sortOrder, String fieldName) {
        if (sortOrder == null) return;
        if (sortOrder == SortOrder.ASC) {
            queryBuilder.query((root, query, criteriaBuilder) ->  {
                query.orderBy(criteriaBuilder.asc(root.get(fieldName)));
                return null;
            });
        } else if (sortOrder == SortOrder.DESC) {
            queryBuilder.query((root, query, criteriaBuilder) -> {
                query.orderBy(criteriaBuilder.desc(root.get(fieldName)));
                return null;
            });
        }
    }

}
