package com.trinity.ctc.domain.user.jwt;

import com.trinity.ctc.domain.user.repository.RefreshTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
public class CustomLogoutFilter extends GenericFilterBean {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public CustomLogoutFilter(JWTUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        doFilter((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, filterChain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        log.info("CustomLogoutFilter------------------------------");
        // path 와 method 검증 -> 지나가는 모든 요청 중, 로그아웃 요청 획득
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();

        log.info("[LogoutFilter] - requestURI: {}", requestURI);
        log.info("[LogoutFilter] - requestMethod: {}", requestMethod);

        if (!requestURI.equals("/logout") || !requestMethod.equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        // refresh 토큰 추출
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
                break;
            }
        }

        // refresh null 검증
        // 후에 예외 처리 해주는 것도 좋겠다
        if (refresh == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // 만료 여부 검증
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        // DB에 저장되어 있는 지 확인
        Boolean isExist = refreshTokenRepository.existsByRefreshToken(refresh);
        if (!isExist) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        /* 로그아웃 진행 */
        // Refresh 토큰 DB에서 제거 -> 재발급 방지
        refreshTokenRepository.deleteByRefreshToken(refresh);

        // Refresh 토큰 Cookie 값 0로 변경 -> 쿠키 초기화
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
//        cookie.setSecure(true);

        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
