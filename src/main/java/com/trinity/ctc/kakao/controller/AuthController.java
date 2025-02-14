package com.trinity.ctc.kakao.controller;

import com.trinity.ctc.kakao.dto.KakaoLogoutResponse;
import com.trinity.ctc.kakao.dto.UserLoginResponse;
import com.trinity.ctc.kakao.service.AuthService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/kakao")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public ResponseEntity<?> kakaoLogin(@RequestParam String code) {
        UserLoginResponse response = authService.authenticateWithKakao(code);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> kakaoLogout(@RequestHeader("Authorization") String authorizationHeader) {
        String accessToken = authorizationHeader.replace("Bearer ", "").trim();
        KakaoLogoutResponse logoutResponse = authService.logout(accessToken);
       return ResponseEntity.ok(Map.of(
        "status", "success",
        "id", logoutResponse.getId()));
    }
}
