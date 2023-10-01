package com.vuong.app.dto.user;

import lombok.Data;

@Data
public class UserOptions {
    private UserSortParameter sort;
    private UserFilterParameter filter;
}
