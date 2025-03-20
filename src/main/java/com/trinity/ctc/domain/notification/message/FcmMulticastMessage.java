package com.trinity.ctc.domain.notification.message;

import com.google.firebase.messaging.MulticastMessage;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class FcmMulticastMessage {
    private final MulticastMessage multicastMessage;
    private final List<String> tokens;
    private final Map<String, String> data;

    public FcmMulticastMessage(MulticastMessage multicastMessage, List<String> tokens, Map<String, String> data) {
        this.multicastMessage = multicastMessage;
        this.tokens = tokens;
        this.data = data;
    }
}
