package com.vuong.app.business.discord.controller;

import com.vuong.app.business.discord.model.payload.GetMemberByServerIdRequest;
import com.vuong.app.business.discord.model.payload.GetMembersByServerIdRequest;
import com.vuong.app.business.discord.service.MemberService;
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
@RequestMapping("/api/v1/discord-service/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/getMembersByServerId")
    public ResponseEntity<?> getMembersByServerId(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody GetMembersByServerIdRequest request) {
        return ResponseEntity.ok(this.memberService.getMembersByServerId(currentUser, request));
    }

    @PostMapping("/getMemberByServerId")
    public ResponseEntity<?> getMemberByServerId(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody GetMemberByServerIdRequest request) {
        return ResponseEntity.ok(this.memberService.getMemberByServerId(currentUser, request));
    }

}
