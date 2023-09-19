package com.vuong.app.operator;

import com.vuong.app.v1.operator.OperatorProto;

import java.util.Date;

public class OperatorGrpcUtil {

    public static String parseNumber(Float f) {
        if (f == null) {
            return "%";
        }

        return String.valueOf(f);
    }

    public static String parseString(String s) {
        if (s == null) {
            return "%";
        }

        return s;
    }

    public static String parseDate(Date d) {
        if (d == null) {
            return "%";
        }

        return d.toString();
    }

    public static int parseBoolean(Boolean b) {
        if (Boolean.TRUE == b) {
            return 1;
        }

        if (Boolean.FALSE == b) {
            return 0;
        }

        return -1;
    }

    public static OperatorProto.NumberRange parseNumberRange(NumberRange numberRange) {
        if (numberRange == null) {
            numberRange = new NumberRange();
        }

        String start = parseNumber(numberRange.getStart());
        String end = parseNumber(numberRange.getEnd());

        return OperatorProto.NumberRange.newBuilder()
                .setStart(start)
                .setEnd(end)
                .build();
    }

    public static OperatorProto.NumberOperators parseNumberOperators(NumberOperators numberOperators) {
        if (numberOperators == null) {
            numberOperators = new NumberOperators();
        }

        String eq = parseNumber(numberOperators.getEq());
        String lt = parseNumber(numberOperators.getLt());
        String lte = parseNumber(numberOperators.getLte());
        String gt = parseNumber(numberOperators.getGt());
        String gte = parseNumber(numberOperators.getGte());
        OperatorProto.NumberRange between = parseNumberRange(numberOperators.getBetween());

        return OperatorProto.NumberOperators.newBuilder()
                .setEq(eq)
                .setLt(lt)
                .setLte(lte)
                .setGt(gt)
                .setGte(gte)
                .setBetween(between)
                .build();
    }

    public static OperatorProto.DateOperators parseDateOperators(DateOperators dateOperators) {
        if (dateOperators == null) {
            dateOperators = new DateOperators();
        }

        String eq = parseDate(dateOperators.getEq());
        String before = parseDate(dateOperators.getBefore());
        String after = parseDate(dateOperators.getAfter());
        OperatorProto.DateRange between = parseDateRange(dateOperators.getBetween());

        return OperatorProto.DateOperators.newBuilder()
                .setEq(eq)
                .setBefore(before)
                .setAfter(after)
                .setBetween(between)
                .build();
    }

    public static OperatorProto.DateRange parseDateRange(DateRange dateRange) {
        if (dateRange == null) {
            dateRange = new DateRange();
        }

        String start = parseDate(dateRange.getStart());
        String end = parseDate(dateRange.getEnd());

        return OperatorProto.DateRange.newBuilder()
                .setStart(start)
                .setEnd(end)
                .build();
    }

    public static OperatorProto.BooleanOperators parseBooleanOperators(BooleanOperators booleanOperators) {
        if (booleanOperators == null) {
            booleanOperators = new BooleanOperators();
        }
        int eq = parseBoolean(booleanOperators.getEq());

        return OperatorProto.BooleanOperators.newBuilder()
                .setEq(eq)
                .build();
    }

    public static OperatorProto.StringOperators parseStringOperators(StringOperators stringOperators) {
        if (stringOperators == null) {
            stringOperators = new StringOperators();
        }

        String eq = parseString(stringOperators.getEq());
        String contains = parseString(stringOperators.getContains());

        return OperatorProto.StringOperators.newBuilder()
                .setEq(eq)
                .setContains(contains)
                .build();
    }

    public static OperatorProto.SortOrder parseSortOrder(SortOrder sortOrder) {
        if (sortOrder == SortOrder.ASC) {
            return OperatorProto.SortOrder.ASC;
        }

        if (sortOrder == SortOrder.DESC) {
            return OperatorProto.SortOrder.DESC;
        }

        return OperatorProto.SortOrder.SORT_ORDER_UNSPECIFIED;
    }

}
