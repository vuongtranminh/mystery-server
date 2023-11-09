package com.vuong.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CreateProfileRequest {
    private String profileId;
    private String name;
    private String avtUrl;
}
