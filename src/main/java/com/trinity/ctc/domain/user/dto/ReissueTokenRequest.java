package com.trinity.ctc.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@Schema(description = "토큰 재발급 by 리프레시 토큰")
public class ReissueTokenRequest {

    @Schema(description = "리프레시 토큰")
    private String refresh;

    public ReissueTokenRequest (String refresh) {
        this.refresh = refresh;
    }
}
