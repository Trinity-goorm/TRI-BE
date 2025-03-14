package com.trinity.ctc.global.kakao.service;

import com.trinity.ctc.global.kakao.config.KakaoApiProperties;
import com.trinity.ctc.global.kakao.dto.KakaoLogoutResponse;
import com.trinity.ctc.global.kakao.dto.KakaoTokenResponse;
import com.trinity.ctc.global.kakao.dto.KakaoUserInfoResponse;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.KakaoErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
public class KakaoApiService {

    private static final Logger log = LoggerFactory.getLogger(KakaoApiService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final KakaoApiProperties kakaoApiProperties;

    public KakaoApiService(KakaoApiProperties kakaoApiProperties) {
        this.kakaoApiProperties = kakaoApiProperties;
    }

    /**
     * 카카오 엑세스토큰 발급
     * @param authorizationCode
     * @return
     */
    public KakaoTokenResponse getAccessToken(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("grant_type", kakaoApiProperties.getGrantType());
        body.add("client_id", kakaoApiProperties.getClientId());
        body.add("redirect_uri", kakaoApiProperties.getRedirectUri());
        body.add("code", authorizationCode);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        KakaoTokenResponse tokenResponse = restTemplate.postForObject(
                kakaoApiProperties.getTokenUrl(),
                requestEntity,
                KakaoTokenResponse.class
        );

        return tokenResponse;
    }

    /**
     * 카카오 ID 획득
     * @param accessToken
     * @return
     */
    public KakaoUserInfoResponse getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        KakaoUserInfoResponse kakaoUserInfoResponse = restTemplate.postForObject(
                kakaoApiProperties.getUserInfoUrl(),
                requestEntity,
                KakaoUserInfoResponse.class
        );
        return kakaoUserInfoResponse;
    }

    /**
     * 로그아웃을 위한 토큰 삭제
     * @param accessToken
     * @return
     */
    public KakaoLogoutResponse deleteToken(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            // 정상적인 로그아웃 요청 수행
            ResponseEntity<KakaoLogoutResponse> response = restTemplate.exchange(
                    kakaoApiProperties.getLogoutUrl(),
                    HttpMethod.POST,
                    requestEntity,
                    KakaoLogoutResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            // 401 Unauthorized (토큰 만료 또는 유효하지 않음) → 로그아웃 성공으로 처리
            if (!isAccessTokenValid(accessToken)) {
                log.warn("유효하지 않은 액세스 토큰으로 로그아웃 요청 → 로그아웃 성공으로 처리");
                return KakaoLogoutResponse.of(-1L);
            }
            throw new CustomException(KakaoErrorCode.LOGOUT_REQUEST_FAILED);
        } catch (ResourceAccessException e) {
            throw new CustomException(KakaoErrorCode.NETWORK_ERROR);
        } catch (Exception e) {
            throw new CustomException(KakaoErrorCode.UNKNOWN_LOGOUT_ERROR);
        }
    }


    /**
     * 엑세스토큰 유효성 검증
     * @param accessToken
     * @return
     */
    public boolean isAccessTokenValid(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(
                    kakaoApiProperties.getAccessTokenValidationUrl(),
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );
            return true;  // 유효한 토큰
        } catch (HttpClientErrorException e) {
            return false;  // 토큰이 유효하지 않음
        }
    }

}
