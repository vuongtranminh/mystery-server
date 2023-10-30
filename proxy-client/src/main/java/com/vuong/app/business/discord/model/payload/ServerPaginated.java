package com.vuong.app.business.discord.model.payload;

import com.vuong.app.common.api.PaginatedList;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ServerPaginated implements PaginatedList<ServerDto> {
    private List<ServerDto> items = new ArrayList<>();
    private Integer totalItems;
}
