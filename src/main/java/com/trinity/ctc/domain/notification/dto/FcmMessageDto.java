package com.trinity.ctc.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FcmMessageDto {
    String title;
    String body;
    String url;
}

