package com.vuong.app.operator;

import lombok.Data;

@Data
public abstract class ListOptions {

    protected Integer currentPage;
    protected Integer pageSize;

    public abstract Integer getCurrentPage();

    public abstract Integer getPageSize();


}
