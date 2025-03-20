package com.trinity.ctc.domain.notification.fomatter;

import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.trinity.ctc.domain.notification.message.FcmMulticastMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationMessageUtil {
    public static Message createMessageWithUrl(String title, String body, String url, String token) {
        return Message.builder()
                .putData("title", title)
                .putData("body", body)
                .putData("url", url)
                .setToken(token)
                .build();
    }

    public static FcmMulticastMessage createMulticastMessageWithUrl(String title, String body, String url, List<String> tokenList) {
        MulticastMessage multicastMessage = MulticastMessage.builder()
                .putData("title", title)
                .putData("body", body)
                .putData("url", url)
                .addAllTokens(tokenList)
                .build();
        Map<String, String> data = new HashMap<>();
        data.put("title", title);
        data.put("body", body);
        data.put("url", url);

        return new FcmMulticastMessage(multicastMessage, tokenList, data);
    }
}
