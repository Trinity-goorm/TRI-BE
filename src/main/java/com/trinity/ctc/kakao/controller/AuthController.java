package com.trinity.ctc.kakao.controller;

import com.trinity.ctc.kakao.service.AuthService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/kakao/login")
    public Map<String, String> kakaoCallback(@RequestParam String code) {
        authService.authenticateWithKakao(code);
        return Map.of("status", "success");
    }
}
