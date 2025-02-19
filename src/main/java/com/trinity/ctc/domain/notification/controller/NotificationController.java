package com.trinity.ctc.domain.notification.controller;

import com.trinity.ctc.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "알림 관련 API")
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/seats/subscribe")
    @Operation(
            summary = "빈자리 알림 신청",
            description = "빈자리 알림 신청 요청 시, 해당 빈자리에 대한 사용자의 알림 구독 정보를 DB에 저장"
    )
    @ApiResponse(
            responseCode = "204",
            description = "성공"
    )
    @ApiResponse(
            responseCode = "409",
            description = "이미 구독 중인 경우"
    )
    @ApiResponse(
            responseCode = "509",
            description = "빈자리 알림 신청 티켓이 부족한 경우"
    )
    public ResponseEntity<Void> subscribeSeatNotification(@RequestParam long seatId, @RequestParam long userId) {
        notificationService.subscribeSeatNotification(seatId, userId);
        return ResponseEntity.noContent().build();
    }
}
