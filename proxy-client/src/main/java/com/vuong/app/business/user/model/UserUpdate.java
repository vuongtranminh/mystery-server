package com.vuong.app.business.user.model;

import com.vuong.app.common.Node;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class UserUpdate implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private String avatar;
    private String bio;
}
