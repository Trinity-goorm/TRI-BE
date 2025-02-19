package com.trinity.ctc.domain.notification.dto;

import com.trinity.ctc.domain.seat.dto.GroupedTimeSlotResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "알림 신청 좌석 리스트 정보")
public class SubscriptionListResponse {
    @Schema(description = "빈자리 알림 신청 건수", example = "5")
    private int subscriptionCount;

    @Schema(description = "빈자리 알림 리스트", implementation = SubscriptionResponse.class)
    private List<SubscriptionResponse> subscriptionList;
}
