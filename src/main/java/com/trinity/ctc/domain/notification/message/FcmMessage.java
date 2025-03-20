package com.trinity.ctc.domain.notification.message;

import com.google.firebase.messaging.Message;
import lombok.Getter;

import java.util.Map;

@Getter
public class FcmMessage {
    private final Message message;
    private final String token;
    private final Map<String, String> data;

    public FcmMessage(Message message, String token, Map<String, String> data) {
        this.message = message;
        this.token = token;
        this.data = data;
    }
}
