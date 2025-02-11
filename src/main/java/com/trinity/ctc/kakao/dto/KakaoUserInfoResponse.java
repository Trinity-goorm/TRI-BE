package com.trinity.ctc.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoUserInfoResponse {
    @JsonProperty("id")
    private String id;
//    @JsonProperty("kakao_account")
//    private KakaoAccount kakaoAccount;


//    @Getter
//    public static class KakaoAccount {
//        @JsonProperty("email")
//        private String email;
//        @JsonProperty("name")
//        private String name;
//        @JsonProperty("gender")
//        private String gender;
//        @JsonProperty("birthday")
//        private String birthday;
//        @JsonProperty("age_range")
//        private String ageRange;
//        @JsonProperty("phone_number")
//        private String phoneNumber;
//
//    }
}
