package com.trinity.ctc.user.service;

import com.trinity.ctc.user.dto.KakaoTokenResponse;
import com.trinity.ctc.user.dto.KakaoUserInfoResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final KakaoApiService kakaoApiService;

    public AuthService(KakaoApiService kakaoApiService) {
        this.kakaoApiService = kakaoApiService;
    }

    public void authenticateWithKakao(String authorizationCode) {
        // 1. 토큰 발급 요청
        KakaoTokenResponse tokenResponse = kakaoApiService.getAccessToken(authorizationCode);
        System.out.println(tokenResponse);

        // 2. 사용자 정보 요청
        KakaoUserInfoResponse userInfo = kakaoApiService.getUserInfo(tokenResponse.getAccessToken());

        // 3. 사용자 정보 처리 (회원 가입 또는 로그인 처리)
        handleUserInfo(userInfo);
    }

    private void handleUserInfo(KakaoUserInfoResponse userInfo) {
        String kakaoId = userInfo.getId();
        String nickname = userInfo.getProperties().get("nickname");
        String email = userInfo.getKakaoAccount().getEmail();

        if (isExistingMember(kakaoId)) {
            createLoginSession(kakaoId);
        } else {
            registerNewMember(kakaoId, nickname, email);
            createLoginSession(kakaoId);
        }
    }

    private boolean isExistingMember(String kakaoId) {
        // 회원 확인 로직
        return false;
    }

    private void registerNewMember(String kakaoId, String nickname, String email) {
        // 신규 회원 등록 로직
    }

    private void createLoginSession(String kakaoId) {
        // 로그인 세션 생성 로직
    }
}
