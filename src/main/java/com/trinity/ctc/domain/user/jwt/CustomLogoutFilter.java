package com.trinity.ctc.domain.user.jwt;

import com.trinity.ctc.domain.user.repository.RefreshTokenRepository;
import com.trinity.ctc.global.kakao.dto.KakaoLogoutResponse;
import com.trinity.ctc.global.kakao.service.KakaoApiService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final KakaoApiService kakaoApiService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        log.info("CustomLogoutFilter------------------------------");

        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();

        log.info("[LogoutFilter] - requestURI: {}", requestURI);
        log.info("[LogoutFilter] - requestMethod: {}", requestMethod);

        // 🚀 로그아웃 요청이 아니면 필터 통과하도록 수정
        if (!requestURI.equals("/logout") || !"POST".equals(requestMethod)) {
            log.info("=========로그아웃 패스!==========");
            filterChain.doFilter(request, response);
            return;
        }

        log.info("=======로그아웃 시작!======");

        // Refresh 토큰 추출
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                    break;
                }
            }
        }

        // Refresh 토큰이 없을 경우
        if (refresh == null) {
            log.warn("🚨 Refresh token is missing.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Refresh 토큰 만료 여부 확인
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            log.warn("🚨 Refresh token is expired.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // DB에 Refresh 토큰이 존재하는지 확인
        boolean isExist = refreshTokenRepository.existsByRefreshToken(refresh);
        if (!isExist) {
            log.warn("🚨 Refresh token not found in database.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 🚀 로그아웃 처리
        refreshTokenRepository.deleteByRefreshToken(refresh);

        response.setStatus(HttpServletResponse.SC_OK);
        log.info("✅ Successfully logged out.");
    }
}
