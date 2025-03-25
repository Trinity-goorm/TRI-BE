package com.trinity.ctc.domain.notification.message;

import com.google.firebase.messaging.MulticastMessage;
import com.trinity.ctc.domain.fcm.entity.Fcm;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class FcmMulticastMessage {
    private final MulticastMessage multicastMessage;
    private final List<Fcm> fcmList;
    private final Map<String, String> data;

    @Builder
    public FcmMulticastMessage(MulticastMessage multicastMessage, List<Fcm> fcmList, Map<String, String> data) {
        this.multicastMessage = multicastMessage;
        this.fcmList = fcmList;
        this.data = data;
    }
}
