package com.vuong.app.operator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class NumberOperators {
    private Float eq;
    private Float notEq;
    private Float lt;
    private Float lte;
    private Float gt;
    private Float gte;
    private NumberRange between;
}
