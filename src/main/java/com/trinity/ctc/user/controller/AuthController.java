package com.trinity.ctc.user.controller;

import com.trinity.ctc.user.service.AuthService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/kakao/callback")
    public Map<String, String> kakaoCallback(@RequestParam String code) {
        authService.authenticateWithKakao(code);
        return Map.of("status", "success", "message", "로그인 성공");
    }
}
