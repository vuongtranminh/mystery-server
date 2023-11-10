package com.vuong.app.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UpdateUserEvent implements UserEvent {
    private String userId;
    private String name;
    private String avtUrl;
}
