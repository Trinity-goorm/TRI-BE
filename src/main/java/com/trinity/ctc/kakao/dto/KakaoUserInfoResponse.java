package com.trinity.ctc.user.dto;

import java.util.Map;

public class KakaoUserInfoResponse {
    private String id;
    private Map<String, String> properties;
    private KakaoAccount kakaoAccount;

    public String getId() {
        return id;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public KakaoAccount getKakaoAccount() {
        return kakaoAccount;
    }

    public static class KakaoAccount {
        private String email;

        public String getEmail() {
            return email;
        }
    }
}
