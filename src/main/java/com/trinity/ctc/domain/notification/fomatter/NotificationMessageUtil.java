package com.trinity.ctc.domain.notification.fomatter;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotification;

import java.util.List;

public class NotificationMessageUtil {
    public static Message createMessageWithUrl(String title, String body, String url, String token) {
        return Message.builder()
                .putData("title", title)
                .putData("body", body)
                .putData("url", url)
                .setToken(token)
                .build();
    }

    public static MulticastMessage createMulticastMessageWithUrl(String title, String body, String url, List<String> tokenList) {
        return MulticastMessage.builder()
                .putData("title", title)
                .putData("body", body)
                .putData("url", url)
                .addAllTokens(tokenList)
                .build();
    }
}
