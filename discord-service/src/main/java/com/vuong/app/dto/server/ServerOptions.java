package com.vuong.app.dto.server;

import lombok.Data;

@Data
public class ServerOptions {
    private ServerSortParameter sort;
    private ServerFilterParameter filter;
}
