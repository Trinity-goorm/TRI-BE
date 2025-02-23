package com.trinity.ctc.domain.notification.dto;

import com.trinity.ctc.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FcmMessageDto {
    String fcmToken;
    String title;
    String body;
    String url;
    User user;

    public FcmMessageDto(String title, String body, String url) {
        this.title = title;
        this.body = body;
        this.url = url;
    }

    public static FcmMessageDto of(FcmMessageDto fcmMessageDto, String fcmToken, User user) {
        return new FcmMessageDto(fcmToken, fcmMessageDto.getTitle(), fcmMessageDto.getBody(), fcmMessageDto.getUrl(), user);
    }
}

