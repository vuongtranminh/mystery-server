package com.vuong.app.business.discord.controller;

import com.vuong.app.business.discord.model.payload.CreateMessageRequest;
import com.vuong.app.business.discord.model.payload.DeleteMessageRequest;
import com.vuong.app.business.discord.model.payload.GetMessagesByChannelIdRequest;
import com.vuong.app.business.discord.model.payload.UpdateMessageRequest;
import com.vuong.app.business.discord.service.MessageService;
import com.vuong.app.security.CurrentUser;
import com.vuong.app.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/createMessage")
    public ResponseEntity<?> createMessage(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody CreateMessageRequest request) {
        return ResponseEntity.ok(this.messageService.createMessage(currentUser, request));
    }

    @PostMapping("/updateMessage")
    public ResponseEntity<?> updateMessage(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UpdateMessageRequest request) {
        return ResponseEntity.ok(this.messageService.updateMessage(currentUser, request));
    }

    @PostMapping("/deleteMessage")
    public ResponseEntity<?> deleteMessage(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody DeleteMessageRequest request) {
        return ResponseEntity.ok(this.messageService.deleteMessage(currentUser, request));
    }

    @PostMapping("/getMessagesByChannelId")
    public ResponseEntity<?> getMessagesByChannelId(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody GetMessagesByChannelIdRequest request) {
        return ResponseEntity.ok(this.messageService.getMessagesByChannelId(currentUser, request));
    }
}
