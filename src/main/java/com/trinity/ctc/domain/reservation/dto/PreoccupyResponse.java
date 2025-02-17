package com.trinity.ctc.domain.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Schema
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PreoccupyResponse {

    @Schema(description = "요청 성공 여부", example = "true")
    private boolean isSuccess;

    @Schema(description = "생성된 예약정보 ID", example = "1")
    private long reservationId;


    public static PreoccupyResponse of(boolean isSuccess, long reservationId) {
        return new PreoccupyResponse(isSuccess, reservationId);
    }
}
