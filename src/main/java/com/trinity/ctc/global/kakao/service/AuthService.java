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

    /**
     * 카카오 인증 구현 (미사용)
     * @param authorizationCode
     * @return
     */
    @Transactional
    public UserLoginResponse authenticateWithKakao(String authorizationCode) {

        KakaoTokenResponse tokenResponse = kakaoApiService.getAccessToken(authorizationCode);
        KakaoUserInfoResponse userInfo = kakaoApiService.getUserInfo(tokenResponse.getAccessToken());

        return handleUserInfo(userInfo, tokenResponse);
    }

    /**
     * 로그인 정보 반환 (구버전) -> 현재는 미사용
     * @param userInfo
     * @param tokenResponse
     * @return
     */
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


    /**
     * 구버전 임시회원가입
     * @param kakaoId
     * @return
     */
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

    /**
     * 로그인 세션 생성
     */
    private void createLoginSession(User user) {
        // Spring Security를 사용하여 인증 세션 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    /**
     * 카카오 엑세스 토큰을 받아 카카오 로그아웃
     */
    public KakaoLogoutResponse logout(String accessToken) {
        KakaoLogoutResponse logoutResponse = kakaoApiService.deleteToken(accessToken);
        return logoutResponse;
    }

    /**
     * 현재 사용자 kakaoId 반환
     * @return kakaoId
     */
    public String getAuthenticatedKakaoId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new CustomException(UserErrorCode.UNAUTHENTICATED);
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

    /**
     * 임시 회원가입 진행
     * @param kakaoId
     * @return 임시회원
     */
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
