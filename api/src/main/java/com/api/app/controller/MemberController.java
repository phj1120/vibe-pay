package com.api.app.controller;

import com.api.app.member.request.LoginRequestDto;
import com.api.app.member.request.SignupRequestDto;
import com.api.app.member.response.TokenResponseDto;
import com.api.app.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody SignupRequestDto request) {
        memberService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto request) {
        TokenResponseDto tokenResponse = memberService.login(request);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        TokenResponseDto tokenResponse = memberService.refreshToken(refreshToken);
        return ResponseEntity.ok(tokenResponse);
    }
}
