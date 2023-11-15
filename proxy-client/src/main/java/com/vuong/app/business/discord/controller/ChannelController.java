package com.vuong.app.business.discord.controller;

import com.vuong.app.business.discord.model.payload.*;
import com.vuong.app.business.discord.service.ChannelService;
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
@RequestMapping("/api/v1/discord-service/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping("/createChannel")
    public ResponseEntity<?> createChannel(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody CreateChannelRequest request) {
        return ResponseEntity.ok(this.channelService.createChannel(currentUser, request));
    }

    @PostMapping("/updateChannel")
    public ResponseEntity<?> updateChannel(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UpdateChannelRequest request) {
        return ResponseEntity.ok(this.channelService.updateChannel(currentUser, request));
    }

    @PostMapping("/deleteChannel")
    public ResponseEntity<?> deleteChannel(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody DeleteChannelRequest request) {
        return ResponseEntity.ok(this.channelService.deleteChannel(currentUser, request));
    }

    @PostMapping("/getChannelGeneralByServerId")
    public ResponseEntity<?> getChannelGeneralByServerId(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody GetChannelGeneralByServerIdRequest request) {
        return ResponseEntity.ok(this.channelService.getChannelGeneralByServerId(currentUser, request));
    }

    @PostMapping("/getChannelByChannelId")
    public ResponseEntity<?> getChannelByChannelId(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody GetChannelByChannelIdRequest request) {
        return ResponseEntity.ok(this.channelService.getChannelByChannelId(currentUser, request));
    }

    @PostMapping("/getChannelsByServerId")
    public ResponseEntity<?> getChannelsByServerId(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody GetChannelsByServerIdRequest request) {
        return ResponseEntity.ok(this.channelService.getChannelsByServerId(currentUser, request));
    }
}
