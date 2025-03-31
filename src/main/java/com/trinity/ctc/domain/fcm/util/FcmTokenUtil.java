package com.trinity.ctc.domain.fcm.util;

public class FcmTokenUtil {
    public static String extractTokenPrefix(String fcmToken) {
        return fcmToken.split(":")[0]; // 혹시 모를 예외에 대비해 유효성 검사도 같이 해줘도 좋음
    }
}
