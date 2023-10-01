package com.vuong.app.consumer;

import com.vuong.app.event.NotificationEmailEvent;
import com.vuong.app.model.NotificationEmail;
import com.vuong.app.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaMessageListener {

    private final MailService mailService;

    @KafkaListener(topics = "userTopic")
    public void handleNotification(NotificationEmailEvent notificationEmailEvent) {
        // send out an email notification
        log.info("Received Notification for notificationEmail - {}", notificationEmailEvent.getSubject());
        mailService.sendMail(NotificationEmail.builder()
                .subject(notificationEmailEvent.getSubject())
                .recipient(notificationEmailEvent.getRecipient())
                .body(notificationEmailEvent.getBody())
                .build());
    }
}
