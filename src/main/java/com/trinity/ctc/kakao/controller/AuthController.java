package com.trinity.ctc.kakao.controller;

import com.trinity.ctc.domain.fcm.dto.FcmTokenRequest;
import com.trinity.ctc.domain.fcm.service.FcmService;
import com.trinity.ctc.kakao.dto.KakaoLogoutResponse;
import com.trinity.ctc.kakao.dto.UserLoginResponse;
import com.trinity.ctc.kakao.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/kakao")
public class AuthController {

    private final AuthService authService;
    private final FcmService fcmService;


    @PostMapping("/login")
    public ResponseEntity<?> kakaoLogin(@RequestParam String code, @RequestBody FcmTokenRequest fcmTokenRequest) {
        UserLoginResponse response = authService.authenticateWithKakao(code);

        fcmService.registerFcmToken(fcmTokenRequest, response.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> kakaoLogout(@RequestHeader("Authorization") String authorizationHeader, @RequestBody FcmTokenRequest fcmTokenRequest) {
        String accessToken = authorizationHeader.replace("Bearer ", "").trim();
        KakaoLogoutResponse logoutResponse = authService.logout(accessToken);

        fcmService.deleteFcmToken(fcmTokenRequest);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "id", logoutResponse.getId()));
    }
}
