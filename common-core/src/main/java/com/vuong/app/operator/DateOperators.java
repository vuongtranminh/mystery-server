package com.vuong.app.operator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DateOperators {
    private Date eq;
    private Date before;
    private Date after;
    private DateRange between;
}

