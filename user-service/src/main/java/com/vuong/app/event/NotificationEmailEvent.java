package com.vuong.app.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NotificationEmailEvent {
    private String subject;
    private String recipient;
    private String body;
}
