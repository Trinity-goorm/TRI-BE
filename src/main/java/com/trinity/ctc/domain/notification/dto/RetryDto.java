package com.trinity.ctc.domain.notification.dto;

import lombok.Getter;

import java.util.Map;

@Getter
public class RetryDto {
    private final String token;
    private final Map<String, String> data;

    public RetryDto(String token, Map<String, String> data) {
        this.token = token;
        this.data = data;
    }
}
