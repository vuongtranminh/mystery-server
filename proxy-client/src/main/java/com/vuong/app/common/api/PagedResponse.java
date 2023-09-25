package com.vuong.app.common.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PagedResponse<T> {
    private Meta meta;

    private List<T> content;

    public static <T> PagedResponse<T> from(Meta meta, List<T> content) {
        content = meta.getTotalElements() == 0 ? Collections.emptyList() : content;
        PagedResponse<T> pagedResponse = new PagedResponse<T>();
        pagedResponse.setMeta(meta);
        pagedResponse.setContent(content);

        return pagedResponse;
    }

    public List<T> getContent() {
        return content == null ? null : new ArrayList<>();
    }

    public final void setContent(List<T> content) {
        this.content = content == null ? null : Collections.unmodifiableList(content);
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }
}
