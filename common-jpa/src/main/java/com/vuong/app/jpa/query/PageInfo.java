package com.vuong.app.jpa.query;

import lombok.Builder;

@Builder
public class PageInfo {
    public Integer current;
    public Integer size;
}
