package com.trinity.ctc.domain.fcm.controller;

import com.trinity.ctc.domain.fcm.dto.FcmTokenRequest;
import com.trinity.ctc.domain.fcm.service.FcmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcmTokens")
public class FcmController {
    private final FcmService fcmService;

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
