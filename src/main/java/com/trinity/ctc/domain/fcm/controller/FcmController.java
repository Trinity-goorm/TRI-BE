package com.trinity.ctc.domain.fcm.controller;

import com.trinity.ctc.domain.fcm.dto.FcmTokenRequest;
import com.trinity.ctc.domain.fcm.service.FcmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcmTokens")
public class FcmController {
    private final FcmService fcmService;

    @PostMapping("/register")
    @Operation(
            summary = "fcm 토큰 등록",
            description = "최초 로그인 시, fcm 토큰 정보 등록 요청"
    )
    @ApiResponse(
            responseCode = "204",
            description = "등록 성공"
    )
    public ResponseEntity<Void> registerFcmToken(@RequestBody FcmTokenRequest fcmTokenRequest, HttpRequest request) {
//        long userId = request.getId();
//
//        fcmService.registerFcmToken(fcmTokenRequest, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete")
    @Operation(
            summary = "fcm 토큰 삭제",
            description = "로그아웃/로그인 세션 만료 시, fcm 토큰 정보 삭제 요청"
    )
    @ApiResponse(
            responseCode = "204",
            description = "삭제 성공"
    )
    public ResponseEntity<Void> deleteFcmToken(@RequestBody FcmTokenRequest fcmTokenRequest) {
//        long userId = request.getId();
//
//        fcmService.deleteFcmToken(fcmTokenRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/renew")
    @Operation(
            summary = "fcm 토큰 갱신",
            description = "로그인 세션이 유지되는 동안 브라우저 재접속 시, fcm 토큰 정보 갱신 요청"
    )
    @ApiResponse(
            responseCode = "204",
            description = "갱신 성공"
    )
    public ResponseEntity<Void> renewFcmToken(@RequestBody FcmTokenRequest fcmTokenRequest) {
        fcmService.renewFcmToken(fcmTokenRequest);
        return ResponseEntity.noContent().build();
    }
}
