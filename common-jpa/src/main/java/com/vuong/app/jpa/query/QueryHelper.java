package com.vuong.app.jpa.query;

import com.vuong.app.operator.*;
import com.vuong.app.v1.operator.OperatorProto;
import org.springframework.util.StringUtils;

public class QueryHelper {

    public static void buildOneStringOperatorFilter(QueryBuilder queryBuilder, OperatorProto.StringOperators stringOperatorsProto,
                                                    String fieldName) {
        StringOperators stringOperators = OperatorClientUtil.parseStringOperators(stringOperatorsProto);

        if (stringOperators == null) return;
        if (!StringUtils.isEmpty(stringOperators.getEq())) {
            queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(fieldName), stringOperators.getEq()));
        } else if (!StringUtils.isEmpty(stringOperators.getContains())) {
            queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.like(root.get(fieldName), "%" + stringOperators.getContains() + "%"));
        }
    }

    public static void buildOneDateOperatorFilter(QueryBuilder queryBuilder, OperatorProto.DateOperators dateOperatorsProto,
                                                  String fieldName) {
        DateOperators dateOperators = OperatorClientUtil.parseDateOperators(dateOperatorsProto);

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

    public static void buildOneBooleanOperatorFilter(QueryBuilder queryBuilder, OperatorProto.BooleanOperators booleanOperatorsProto,
                                                     String fieldName) {
        BooleanOperators booleanOperators = OperatorClientUtil.parseBooleanOperators(booleanOperatorsProto);

        if (booleanOperators == null) return;
        if (booleanOperators.getEq() != null) {
            queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(fieldName), booleanOperators.getEq()));
        }
    }

    public static void buildOneNumberOperatorFilter(QueryBuilder queryBuilder, OperatorProto.NumberOperators numberOperatorsProto,
                                                    String fieldName) {
        NumberOperators numberOperators = OperatorClientUtil.parseNumberOperators(numberOperatorsProto);

        if (numberOperators == null) return;
        if (numberOperators.getEq() != null) {
            queryBuilder.query((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(fieldName), numberOperators.getEq()));
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

    public static void buildOneSortOrder(QueryBuilder queryBuilder, OperatorProto.SortOrder sortOrderProto, String fieldName) {
        SortOrder sortOrder = OperatorClientUtil.parseSortOrder(sortOrderProto);

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
