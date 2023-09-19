package com.vuong.app.operator;

import com.vuong.app.v1.operator.OperatorProto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OperatorClientUtil {

    public static Float parseNumber(String s) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String parseString(String s) {
        if (("%").equals(s)) {
            return null;
        }

        return s;
    }

    public static Date parseDate(String s) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Date date = dateFormat.parse(s);
            return date;
        } catch (ParseException e) {
            return null;
        }
    }

    public static Boolean parseBoolean(int i) {
        if (i == 1) {
            return Boolean.TRUE;
        }

        if (i == 0) {
            return Boolean.FALSE;
        }

        return null;
    }

    public static NumberOperators parseNumberOperators(OperatorProto.NumberOperators numberOperators) {
        Float eq = parseNumber(numberOperators.getEq());
        Float lt = parseNumber(numberOperators.getLt());
        Float lte = parseNumber(numberOperators.getLte());
        Float gt = parseNumber(numberOperators.getGt());
        Float gte = parseNumber(numberOperators.getGte());
        NumberRange between = parseNumberRange(numberOperators.getBetween());

        if (eq == null && lt == null && lte == null && gt == null && gte == null && between == null) {
            return null;
        }

        return NumberOperators.builder()
                .eq(eq)
                .lt(lt)
                .lte(lte)
                .gt(gt)
                .gte(gte)
                .between(between)
                .build();
    }

    public static NumberRange parseNumberRange(OperatorProto.NumberRange numberRange) {
        Float start = parseNumber(numberRange.getStart());
        Float end = parseNumber(numberRange.getEnd());

        if (start == null || end == null) {
            return null;
        }

        return NumberRange.builder()
                .start(start)
                .end(end)
                .build();
    }

    public static BooleanOperators parseBooleanOperators(OperatorProto.BooleanOperators booleanOperators) {
        Boolean eq = parseBoolean(booleanOperators.getEq());

        if (eq == null) {
            return null;
        }

        return BooleanOperators.builder()
                .eq(eq)
                .build();
    }

    public static DateOperators parseDateOperators(OperatorProto.DateOperators dateOperators) {
        Date eq = parseDate(dateOperators.getEq());
        Date before = parseDate(dateOperators.getBefore());
        Date after = parseDate(dateOperators.getAfter());
        DateRange between = parseDateRange(dateOperators.getBetween());

        if (eq == null && before == null && after == null && between == null) {
            return null;
        }

        return DateOperators.builder()
                .eq(eq)
                .before(before)
                .after(after)
                .between(between)
                .build();
    }

    public static DateRange parseDateRange(OperatorProto.DateRange dateRange) {
        Date start = parseDate(dateRange.getStart());
        Date end = parseDate(dateRange.getEnd());

        if (start == null || end == null) {
            return null;
        }

        return DateRange.builder()
                .start(start)
                .end(end)
                .build();
    }

    public static SortOrder parseSortOrder(OperatorProto.SortOrder sortOrder) {
        if (sortOrder.compareTo(OperatorProto.SortOrder.ASC) == 0) {
            return SortOrder.ASC;
        }

        if (sortOrder.compareTo(OperatorProto.SortOrder.DESC) == 0) {
            return SortOrder.DESC;
        }

        return null;
    }

    public static StringOperators parseStringOperators(OperatorProto.StringOperators stringOperators) {
        String eq = parseString(stringOperators.getEq());
        String contains = parseString(stringOperators.getContains());

        if (eq == null && contains == null) {
            return null;
        }

        return StringOperators.builder()
                .eq(eq)
                .contains(contains)
                .build();
    }
}
