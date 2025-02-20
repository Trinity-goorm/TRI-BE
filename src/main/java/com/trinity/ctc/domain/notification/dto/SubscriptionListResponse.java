package com.trinity.ctc.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "빈자리 알림 신청 좌석 리스트")
public class SubscriptionListResponse {
    @Schema(description = "빈자리 알림 신청 건수", example = "5")
    private int subscriptionCount;

    @ArraySchema(schema = @Schema(implementation = SubscriptionResponse.class))
    @Schema(description = "빈자리 알림 신청 좌석 정보 리스트")
    private List<SubscriptionResponse> subscriptionList;
}
