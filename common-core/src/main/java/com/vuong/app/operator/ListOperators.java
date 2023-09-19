package com.vuong.app.operator;

import lombok.Data;

import java.util.Collection;

@Data
public class ListOperators<E> {
    private Collection<E> in;
    private Collection<E> notIn;
}
