package com.trinity.ctc.domain.notification.message;

import com.google.firebase.messaging.MulticastMessage;
import com.trinity.ctc.domain.fcm.entity.Fcm;
import com.trinity.ctc.domain.notification.type.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

// Firebase의 MulticastMessage 객체의 wrapper 클래스
@Getter
public class FcmMulticastMessage {
    private final MulticastMessage multicastMessage;
    private final List<Fcm> fcmList;
    private final Map<String, String> data;
    private NotificationType type;

    @Builder
    public FcmMulticastMessage(MulticastMessage multicastMessage, List<Fcm> fcmList, Map<String, String> data, NotificationType type) {
        this.multicastMessage = multicastMessage;
        this.fcmList = fcmList;
        this.data = data;
        this.type = type;
    }
}
