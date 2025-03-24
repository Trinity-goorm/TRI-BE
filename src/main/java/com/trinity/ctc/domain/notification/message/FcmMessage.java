package com.trinity.ctc.domain.notification.message;

import com.google.firebase.messaging.Message;
import com.trinity.ctc.domain.fcm.entity.Fcm;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class FcmMessage {
    private final Message message;
    private final Fcm fcm;
    private final Map<String, String> data;

    @Builder
    public FcmMessage(Message message, Fcm fcm, Map<String, String> data) {
        this.message = message;
        this.fcm = fcm;
        this.data = data;
    }
}
