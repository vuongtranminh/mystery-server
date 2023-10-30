package com.vuong.app.dto;

import java.util.List;

public interface PaginatedList<T> {
    List<T> getItems();
    void setItems(List<T> items);
    Integer getTotalItems();
    void setTotalItems(Integer totalItems);
}
