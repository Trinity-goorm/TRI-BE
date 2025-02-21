package com.trinity.ctc.domain.notification.controller;

import com.trinity.ctc.domain.notification.dto.SubscriptionListResponse;
import com.trinity.ctc.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            description = "신청 성공"
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

    @GetMapping("/seats")
    @Operation(
            summary = "빈자리 알림 신청 리스트 반환",
            description = "사용자의 빈자리 알림 구독 데이터를 모두 반환"
    )
    @ApiResponse(
            responseCode = "200",
            description = "신청 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SubscriptionListResponse.class)
            )
    )
    public ResponseEntity<SubscriptionListResponse> getSeatNotifications(@RequestParam long userId) {
        SubscriptionListResponse subscriptionList = notificationService.getSeatNotifications(userId);
        return ResponseEntity.ok(subscriptionList);
    }

    @DeleteMapping("/seats/cancel")
    @Operation(
            summary = "빈자리 알림 취소",
            description = "빈자리 알림 취소 요청 시, 해당 빈자리에 대한 사용자의 알림 구독 정보를 DB에서 삭제"
    )
    @ApiResponse(
            responseCode = "204",
            description = "취소 성공"
    )
    public ResponseEntity<Void> cancelSubscribeSeatNotification(@RequestParam long seatNotificationId) {
        notificationService.cancelSubscribeSeatNotification(seatNotificationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/test")
    public ResponseEntity<Void> testNotification(@RequestParam long userId, @RequestParam long reservationId) {
        notificationService.testNotification(userId, reservationId);
        return ResponseEntity.noContent().build();
    }
}
