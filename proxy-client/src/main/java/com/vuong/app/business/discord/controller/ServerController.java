package com.vuong.app.business.discord.controller;

import com.vuong.app.business.discord.model.payload.CreateServerRequest;
import com.vuong.app.business.discord.model.payload.GetServerJoinByServerIdRequest;
import com.vuong.app.business.discord.model.payload.GetServersJoinRequest;
import com.vuong.app.business.discord.service.ServerService;
import com.vuong.app.common.api.ResponseObject;
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
@RequestMapping("/api/v1/discord-service/servers")
@RequiredArgsConstructor
public class ServerController {

    private final ServerService serverService;

    @PostMapping("/createServer")
    public ResponseEntity<?> createServer(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody CreateServerRequest request) {
        return ResponseEntity.ok(this.serverService.createServer(currentUser, request));
    }

    @PostMapping("/getServersJoin")
    public ResponseEntity<?> getServersJoin(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody GetServersJoinRequest request) {
        return ResponseEntity.ok(this.serverService.getServersJoin(currentUser, request));
    }

    @PostMapping("/getFirstServerJoin")
    public ResponseEntity<?> getFirstServerJoin(@CurrentUser UserPrincipal currentUser) {
        return ResponseEntity.ok(this.serverService.getFirstServerJoin(currentUser));
    }

    @PostMapping("/getServerJoinByServerId")
    public ResponseEntity<?> getServerJoinByServerId(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody GetServerJoinByServerIdRequest request) {
        return ResponseEntity.ok(this.serverService.getServerJoinByServerId(currentUser, request));
    }
}
