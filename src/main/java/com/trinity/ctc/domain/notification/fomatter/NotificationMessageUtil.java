package com.trinity.ctc.domain.notification.fomatter;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.notification.message.FcmMessage;
import com.trinity.ctc.domain.notification.message.FcmMulticastMessage;
import com.trinity.ctc.domain.user.entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NotificationMessageUtil {

    public static FcmMessage createMessageWithUrl(String title, String body, String url, Fcm fcm) {
        Message message = Message.builder()
                .putData("title", title)
                .putData("body", body)
                .putData("url", url)
                .setToken(fcm.getToken())
                .build();

        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("body", body);
        data.put("url", url);

        return new FcmMessage(message, fcm, data);
    }

    public static FcmMulticastMessage createMulticastMessageWithUrl(String title, String body, String url, List<Fcm> fcmList) {
        MulticastMessage multicastMessage = MulticastMessage.builder()
                .putData("title", title)
                .putData("body", body)
                .putData("url", url)
                .addAllTokens(fcmList.stream().map(Fcm::getToken).collect(Collectors.toList()))
                .build();

        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("body", body);
        data.put("url", url);

        return new FcmMulticastMessage(multicastMessage, fcmList, data);
    }
}
