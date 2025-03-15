package com.trinity.ctc.domain.notification.dto;

import com.trinity.ctc.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class FcmMulticastMessageDto {
    List<String> fcmTokens;
    String title;
    String body;
    String url;
    User user;
}
