package com.trinity.ctc.domain.notification.message;

import com.google.firebase.messaging.Message;
import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.notification.type.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

// Firebase의 Message 객체의 wrapper 클래스
@Getter
public class FcmMessage {
    // 
    private final Message message;
    private final Fcm fcm;
    private final Map<String, String> data;
    private NotificationType type;

    @Builder
    public FcmMessage(Message message, Fcm fcm, Map<String, String> data, NotificationType type) {
        this.message = message;
        this.fcm = fcm;
        this.data = data;
        this.type = type;
    }
}
