package com.trinity.ctc.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "카카오 로그아웃 응답")
public class KakaoLogoutResponse {

    @JsonProperty("id")
    @Schema(description = "사용자 식별자", example = "1")
    private String id;

    @Schema(description = "로그아웃 상태", example = "success")
    private final String status;

    @Builder
    public static KakaoLogoutResponse of(String id, String status) {
        return KakaoLogoutResponse.builder()
            .status(status)
            .id(id)
            .build();
    }
}
