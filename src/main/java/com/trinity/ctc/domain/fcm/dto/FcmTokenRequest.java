package com.trinity.ctc.domain.fcm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "FCM 토큰 정보")
public class FcmTokenRequest {
    @NotNull
    @Schema(description = "FCM 토큰 값", example = "eFWf9agIX28I7RXM3Zy5_G:APA91bGuCc3Do-SZVanV7C39JH465yTDL...")
    private String fcmToken;

    @Schema(description = "토큰 발급 시간 (optional, yyyy-MM-dd HH:mm 형식의 timeStamp, 로그아웃 요청 시 불필요)",
            example = "2024-03-13 13:55")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime timeStamp;
}
