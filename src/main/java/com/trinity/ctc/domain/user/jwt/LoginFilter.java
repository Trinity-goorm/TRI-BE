package com.trinity.ctc.domain.user.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinity.ctc.domain.user.dto.KakaoLoginRequest;
import com.trinity.ctc.domain.user.entity.RefreshToken;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.repository.RefreshTokenRepository;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.domain.user.status.UserStatus;
import com.trinity.ctc.global.kakao.dto.KakaoTokenResponse;
import com.trinity.ctc.global.kakao.dto.KakaoUserInfoResponse;
import com.trinity.ctc.global.kakao.service.AuthService;
import com.trinity.ctc.global.kakao.service.KakaoApiService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final KakaoApiService kakaoApiService;
    private final AuthService authService;

    /**
     * 사용자 아이디와 비밀번호를 받아 인증
     * @param request
     * @param response
     * @return 인증
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        log.info("Attempting to authenticate-------------------------");

        try {
            KakaoLoginRequest kakaoLoginRequest = objectMapper.readValue(request.getInputStream(), KakaoLoginRequest.class);

            return authenticateWithKakao(kakaoLoginRequest.getAuthCode());

        } catch (IOException e) {
            throw new AuthenticationException("Failed to parse authentication request body", e) {};
        }
    }


    private Authentication authenticateWithKakao(String authCode) {
        log.info("Authenticating with Kakao API");

        // 1. 인가 코드로 카카오 액세스 토큰 요청
        KakaoTokenResponse kakaoAccessTokenResponse = kakaoApiService.getAccessToken(authCode);

        // 2. 액세스 토큰으로 사용자 정보 조회
        KakaoUserInfoResponse kakaoUserInfoResponse = kakaoApiService.getUserInfo(kakaoAccessTokenResponse.getAccessToken());

        // 3. 유저 정보 조회 (없으면 임시 사용자 등록)
        User user = userRepository.findByKakaoId(Long.parseLong(kakaoUserInfoResponse.getKakaoId()))
                .orElseGet(() -> authService.registerTempUser(Long.parseLong(kakaoUserInfoResponse.getKakaoId())));

        log.info("✅ Kakao ID: {}, User Status: {}", user.getKakaoId(), user.getStatus());

        // 4. 실제 UserStatus를 기반으로 인증 객체 생성
        return new UsernamePasswordAuthenticationToken(
                kakaoUserInfoResponse.getKakaoId(),
                null,
                List.of(new SimpleGrantedAuthority(user.getStatus().name()))  // ✅ 실제 상태 반영
        );
    }


    /**
     * 인증 성공 시
     * @param request
     * @param response
     * @param chain
     * @param authentication
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException {

        // 유저 정보
        String kakaoId = authentication.getName();
        log.info("Successfully authenticated user's Kakao Id: {}", kakaoId);

        // 기존 회원여부 확인
        User user = userRepository.findByKakaoId(Long.parseLong(kakaoId))
                .orElseGet(() -> authService.registerTempUser(Long.parseLong(kakaoId)));

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> authoritiesIterator = authorities.iterator();
        GrantedAuthority authority = authoritiesIterator.next();
        String status = authority.getAuthority();
        log.info("{} has role: {}", kakaoId, status);

        // 토큰 생성
        String accessToken = jwtUtil.createJwt("access", kakaoId, status, 600000L);
        String refreshToken = jwtUtil.createJwt("refresh", kakaoId, status, 86400000L);

        // Refresh 토큰 저장
        addRefreshToken(kakaoId, refreshToken, 86400000L);

        log.info("🎉 로그인 성공: {}", kakaoId);
        log.info("[LoginFilter] - AccessToken: {}", accessToken);
        log.info("[LoginFilter] - RefreshToken: {}", refreshToken);


        // 응답 설정
        response.setHeader("access", accessToken);
        response.setHeader("refresh", refreshToken);
        response.setStatus(HttpStatus.OK.value());

        // ✅ 응답 JSON 구성
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ObjectMapper objectMapper = new ObjectMapper();
        String responseBody;

        // ✅ 온보딩이 필요한 경우에도 토큰을 발급하고 클라이언트가 처리할 수 있도록 응답
        boolean needOnboarding = status.equals(UserStatus.TEMPORARILY_UNAVAILABLE.name());

        if (needOnboarding) {
            log.warn("🚨 온보딩이 필요한 사용자: {}", kakaoId);
            responseBody = objectMapper.writeValueAsString(new LoginResponse(true, "온보딩이 필요한 사용자입니다."));
        } else {
            responseBody = objectMapper.writeValueAsString(new LoginResponse(false, "로그인 성공"));
        }


        response.getWriter().write(responseBody);
        response.setStatus(HttpStatus.OK.value());
    }

    /**
     * 인증 실패 시
     * @param request
     * @param response
     * @param failed
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {

        log.debug("Unsuccessful authentication");
        response.setStatus(401);
    }

    private void addRefreshToken(String username, String refreshToken, Long expiredMs) {

        Date expiration = new Date(System.currentTimeMillis() + expiredMs);

        RefreshToken refresh = new RefreshToken();
        refresh.setUsername(username);
        refresh.setRefreshToken(refreshToken);
        refresh.setExpiration(expiration.toString());

        refreshTokenRepository.save(refresh);
    }

    /**
     * 로그인 응답 DTO
     */
    public record LoginResponse(boolean needOnboarding, String message) {}
}
