package com.trinity.ctc.domain.user.service;

import com.trinity.ctc.domain.user.entity.RefreshToken;
import com.trinity.ctc.domain.user.jwt.JWTUtil;
import com.trinity.ctc.domain.user.repository.RefreshTokenRepository;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.TokenErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class TokenService {

    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenService(JWTUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }


    @Transactional
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanUpExpiredTokens() {
        Instant deadline = Instant.now().minus(1, ChronoUnit.HOURS);
        Date oneHourAgo = Date.from(deadline);

        List<RefreshToken> expiredRefreshTokens = refreshTokenRepository.findByExpirationBefore(String.valueOf(oneHourAgo));

        if (!expiredRefreshTokens.isEmpty()) {
            refreshTokenRepository.deleteAll(expiredRefreshTokens);
            log.info("Deleted {} expired refresh tokens", expiredRefreshTokens.size());
        } else {
            log.info("No expired refresh tokens found");
        }
    }

    /**
     * Access 토큰 재발급
     * @param request
     * @param response
     * @return 새로운 Access 토큰
     */
    public String reissueToken(HttpServletRequest request, HttpServletResponse response) {

        String refresh = getRefreshTokenFromCookie(request.getCookies());
        log.info("[Reissue Service] - Received refresh token: {}", refresh);

        if (refresh == null) {
            // response status code : 프론트와 협업한 상태코드
            throw new CustomException(TokenErrorCode.REFRESH_TOKEN_IS_NULL);
        }

        // 토큰 만료 확인
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            throw new CustomException(TokenErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        // 토큰 카테고리 확인
        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {
            throw new CustomException(TokenErrorCode.INVALID_TOKEN_CATEGORY);
        }

        // DB에 저장되어 있는 지 확인
        Boolean refreshTokenExist = refreshTokenRepository.existsByRefreshToken(refresh);
        if (!refreshTokenExist) {
            throw new CustomException(TokenErrorCode.INVALID_REFRESH_TOKEN);
        }

        String kakaoId = jwtUtil.getKakaoId(refresh);
        String status = String.valueOf(jwtUtil.getStatus(refresh));

        log.info("[Reissue Service] - username: {}, role: {}", kakaoId, status);

        // 새로운 JWT 생성
        String newAccess = jwtUtil.createJwt("access", kakaoId, status, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", kakaoId, status, 86400000L);
        log.info("[Reissue Service] - newAccess: {}", newAccess);
        log.info("[Reissue Service] - newRefresh: {}", newRefresh);

        // Refresh 토큰 교체
        refreshTokenRepository.deleteByRefreshToken(refresh);
        addRefreshToken(kakaoId, newRefresh, 86400000L);

        // response
        response.setHeader("access", newAccess);
        response.addCookie(createCookie("refresh", newRefresh));

        // 토큰을 굳이 보낼 이유는 없다. 후에 고민
        return newAccess;
    }

    /**
     * Refresh 토큰 교체
     * @param username
     * @param newRefresh
     * @param expiredMs
     */
    private void addRefreshToken(String username, String newRefresh, Long expiredMs) {

        Date expirationDate = new Date(System.currentTimeMillis() + expiredMs);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsername(username);
        refreshToken.setRefreshToken(newRefresh);
        refreshToken.setExpiration(expirationDate.toString());

        refreshTokenRepository.save(refreshToken);
    }

    /**
     * 요청의 Cookie에서 refresh 토큰 추출
     * @param cookies
     * @return
     */
    private String getRefreshTokenFromCookie(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 쿠키 생성
     * @param key
     * @param value
     * @return 생성된 쿠키
     */
    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        cookie.setHttpOnly(true);

//        cookie.setSecure(true);
//        cookie.setPath("/");

        return cookie;
    }
}
