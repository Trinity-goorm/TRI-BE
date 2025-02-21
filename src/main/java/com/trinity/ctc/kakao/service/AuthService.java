package com.trinity.ctc.kakao.service;

import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.kakao.dto.KakaoLogoutResponse;
import com.trinity.ctc.kakao.dto.KakaoTokenResponse;
import com.trinity.ctc.kakao.dto.KakaoUserInfoResponse;
import com.trinity.ctc.kakao.dto.UserLoginResponse;
import com.trinity.ctc.domain.user.repository.UserRepository;
import java.util.Collections;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final KakaoApiService kakaoApiService;
    private final UserRepository userRepository;

    public AuthService(KakaoApiService kakaoApiService, UserRepository userRepository) {
        this.kakaoApiService = kakaoApiService;
        this.userRepository = userRepository;
    }

    public UserLoginResponse authenticateWithKakao(String authorizationCode) {

        // 1. 토큰 발급 요청
        KakaoTokenResponse tokenResponse = kakaoApiService.getAccessToken(authorizationCode);

        // 2. 사용자 정보 요청
        KakaoUserInfoResponse userInfo = kakaoApiService.getUserInfo(tokenResponse.getAccessToken());

        // 3. 사용자 정보 처리 (회원 가입 또는 로그인 처리)
        UserLoginResponse response =  handleUserInfo(userInfo, tokenResponse);

        return response;
    }

    private UserLoginResponse handleUserInfo(KakaoUserInfoResponse userInfo, KakaoTokenResponse tokenResponse) {
        Long kakaoId = Long.parseLong(userInfo.getKakaoId());

        // 회원 존재 여부 확인
        Optional<User> existingUser = userRepository.findByKakaoId(kakaoId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            createLoginSession(user);
            return UserLoginResponse.existingUser(user, tokenResponse);

        } else {
            User newUser = registerNewMember(kakaoId);
            return UserLoginResponse.newUser(newUser,tokenResponse);
        }
    }


    private User registerNewMember(Long kakaoId) {
        User newUser = User.builder()
            .kakaoId(kakaoId)
            .normalTicketCount(100)
            .emptyTicket(10)
            .build();

        userRepository.save(newUser);
        createLoginSession(newUser);
        return newUser;
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
