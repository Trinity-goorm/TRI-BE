package com.trinity.ctc.global.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoUserInfoResponse {
    @JsonProperty("id")
    private String kakaoId;
}
