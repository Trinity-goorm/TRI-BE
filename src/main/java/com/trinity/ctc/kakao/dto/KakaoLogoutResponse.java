package com.trinity.ctc.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoLogoutResponse {

    @JsonProperty("id")
    private String id;
}
