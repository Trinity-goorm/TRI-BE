package com.trinity.ctc.user.controller;

import com.trinity.ctc.user.service.AuthService;
import java.util.Map;
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

    @PostMapping("/kakao/callback")
    public Map<String, String> kakaoCallback(@RequestParam String code) {
        //인가 코드 받기 요청시 성공하면 받을 수 있는 "code"가 필요함. 선택사항이지만, 토큰 발급받기 위해 필요함.
        authService.authenticateWithKakao(code);
        return Map.of("status", "success", "message", "로그인 성공");
    }
}
