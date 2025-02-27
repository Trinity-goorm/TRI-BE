package com.trinity.ctc.global.kakao.service;

import com.trinity.ctc.domain.user.dto.CustomUserDetails;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.domain.user.status.UserStatus;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.UserErrorCode;
import com.trinity.ctc.global.kakao.dto.KakaoLogoutResponse;
import com.trinity.ctc.global.kakao.dto.KakaoTokenResponse;
import com.trinity.ctc.global.kakao.dto.KakaoUserInfoResponse;
import com.trinity.ctc.global.kakao.dto.UserLoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
public class AuthService {

    private final KakaoApiService kakaoApiService;
    private final UserRepository userRepository;

    public AuthService(KakaoApiService kakaoApiService, UserRepository userRepository) {
        this.kakaoApiService = kakaoApiService;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserLoginResponse authenticateWithKakao(String authorizationCode) {

        KakaoTokenResponse tokenResponse = kakaoApiService.getAccessToken(authorizationCode);
        KakaoUserInfoResponse userInfo = kakaoApiService.getUserInfo(tokenResponse.getAccessToken());

        return handleUserInfo(userInfo, tokenResponse);
    }

    private UserLoginResponse handleUserInfo(KakaoUserInfoResponse userInfo, KakaoTokenResponse tokenResponse) {
        Long kakaoId = Long.valueOf(userInfo.getKakaoId());

        // 회원 존재 여부 확인
        User existingUser = userRepository.findByKakaoId(kakaoId).orElse(null);

        if (existingUser != null && existingUser.getStatus() == UserStatus.AVAILABLE) {
            createLoginSession(existingUser);
            return UserLoginResponse.existingUser(existingUser, tokenResponse);

        } else {
            User newUser = registerNewMember(kakaoId);
            return UserLoginResponse.newUser(newUser, tokenResponse);
        }
    }


    private User registerNewMember(Long kakaoId) {
        User newUser = User.builder()
                .kakaoId(kakaoId)
                .normalTicketCount(100)
                .emptyTicket(10)
                .status(UserStatus.TEMPORARILY_UNAVAILABLE)
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

    /**
     * 현재 사용자 이메일 반환
     * @return
     */
    public String getAuthenticatedKakaoId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new CustomException(UserErrorCode.UNAUTHENTICATED);
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    public Long getAuthenticatedUserId() {
        User user = userRepository.findByKakaoId(Long.valueOf(getAuthenticatedKakaoId()))
                .orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));
        return user.getId();
    }

    @Transactional
    public User registerTempUser(Long kakaoId) {
        log.info("임시 회원가입 진행 - Kakao ID: {}", kakaoId);

        User newUser = User.builder()
                .kakaoId(kakaoId)
                .normalTicketCount(100)
                .emptyTicket(10)
                .status(UserStatus.TEMPORARILY_UNAVAILABLE)
                .build();

        return userRepository.save(newUser);
    }
}
