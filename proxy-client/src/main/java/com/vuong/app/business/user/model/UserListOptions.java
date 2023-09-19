package com.vuong.app.business.user.model;

import com.vuong.app.operator.ListOptions;
import lombok.Data;

@Data
public class UserListOptions extends ListOptions {
    private UserSortParameter sort;
    private UserFilterParameter filter;

    @Override
    public Integer getCurrentPage() {
        return currentPage == null ? 0 : currentPage;
    }

    @Override
    public Integer getPageSize() {
        return pageSize == null ? 30 : pageSize;
    }

}
