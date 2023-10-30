package com.vuong.app.business.discord.controller;

import com.vuong.app.business.auth.model.payload.SignInRequest;
import com.vuong.app.business.discord.model.payload.CreateServerRequest;
import com.vuong.app.business.discord.model.payload.DeleteServerRequest;
import com.vuong.app.business.discord.model.payload.UpdateServerRequest;
import com.vuong.app.business.discord.service.ServerService;
import com.vuong.app.common.api.ResponseObject;
import com.vuong.app.security.CurrentUser;
import com.vuong.app.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/server")
@RequiredArgsConstructor
public class ServerController {

    private final ServerService serverService;

    @PostMapping("/signin")
    public ResponseEntity<?> createServer(@Valid @RequestBody CreateServerRequest request, @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(this.serverService.createServer(request, currentUser));
    }

    @PostMapping("/inviteCode/{inviteCode}")
    public ResponseEntity<?> getServerByInviteCode(@PathVariable String inviteCode) {
        return ResponseEntity.ok(this.serverService.getServerByInviteCode(inviteCode));
    }

    @PostMapping("/{serverId}")
    public ResponseEntity<?> getServerByServerId(@PathVariable Integer serverId) {
        return ResponseEntity.ok(this.serverService.getServerByServerId(serverId));
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteServerByServerId(@Valid @RequestBody DeleteServerRequest request, @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(this.serverService.deleteServerByServerId(request, currentUser));
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateServerByServerId(@Valid @RequestBody UpdateServerRequest request, @CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(this.serverService.updateServerByServerId(request, currentUser));
    }

    @PostMapping("/firstServer")
    public ResponseEntity<?> getFirstServer(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(this.serverService.getFirstServer(currentUser));
    }

}
