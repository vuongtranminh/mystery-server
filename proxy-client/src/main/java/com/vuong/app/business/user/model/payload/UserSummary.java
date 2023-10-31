package com.vuong.app.business.user.model.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserSummary {
    private String userId;
    private String name;
    private String avtUrl;
}
