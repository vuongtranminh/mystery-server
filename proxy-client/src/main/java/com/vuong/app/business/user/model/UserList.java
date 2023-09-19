package com.vuong.app.business.user.model;

import com.vuong.app.operator.PaginatedList;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class UserList implements PaginatedList<UserDto> {
    private List<UserDto> items = new ArrayList<>();
    private Integer totalItems;
}
