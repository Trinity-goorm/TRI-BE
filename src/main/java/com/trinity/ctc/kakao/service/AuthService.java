package com.trinity.ctc.kakao.service;

import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.kakao.dto.KakaoLogoutResponse;
import com.trinity.ctc.kakao.dto.KakaoTokenResponse;
import com.trinity.ctc.kakao.dto.KakaoUserInfoResponse;
import com.trinity.ctc.kakao.repository.UserRepository;
import java.util.Collections;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {

    private final KakaoApiService kakaoApiService;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public AuthService(KakaoApiService kakaoApiService, UserRepository userRepository) {
        this.kakaoApiService = kakaoApiService;
        this.userRepository = userRepository;
    }

    public KakaoTokenResponse authenticateWithKakao(String authorizationCode) {

        // 1. 토큰 발급 요청
        KakaoTokenResponse tokenResponse = kakaoApiService.getAccessToken(authorizationCode);

        // 2. 사용자 정보 요청
        KakaoUserInfoResponse userInfo = kakaoApiService.getUserInfo(tokenResponse.getAccessToken());

        // 3. 사용자 정보 처리 (회원 가입 또는 로그인 처리)
        handleUserInfo(userInfo);

        return tokenResponse;
    }

    private void handleUserInfo(KakaoUserInfoResponse userInfo) {
        Long kakaoId = Long.parseLong(userInfo.getId());

        // 회원 존재 여부 확인
        Optional<User> existingUser = userRepository.findByKakaoId(kakaoId);

        if (existingUser.isPresent()) {
            // 기존 회원 -> 로그인 세션 생성
            createLoginSession(existingUser.get());
        } else {
            // 신규 회원 -> 회원 등록 후 세션 생성
            registerNewMember(kakaoId);
        }
    }


    private void registerNewMember(Long kakaoId) {
        User newUser = User.builder()
            .kakaoId(kakaoId)
            .normalTicketCount(100)
            .emptyTicket(10)
            .build();

        userRepository.save(newUser);
        createLoginSession(newUser);
    }

    private void createLoginSession(User user) {
        // Spring Security를 사용하여 인증 세션 생성
        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    public KakaoLogoutResponse logout(String accessToken) {
        KakaoLogoutResponse logoutResponse = kakaoApiService.deleteToken(accessToken);
        return logoutResponse;
    }
}
