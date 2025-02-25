package com.trinity.ctc.kakao.service;

import com.trinity.ctc.kakao.config.KakaoApiProperties;
import com.trinity.ctc.kakao.dto.KakaoLogoutResponse;
import com.trinity.ctc.kakao.dto.KakaoTokenResponse;
import com.trinity.ctc.kakao.dto.KakaoUserInfoResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class KakaoApiService {

    private static final Logger log = LoggerFactory.getLogger(KakaoApiService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final KakaoApiProperties kakaoApiProperties;

    public KakaoApiService(KakaoApiProperties kakaoApiProperties) {
        this.kakaoApiProperties = kakaoApiProperties;
    }

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

    public KakaoLogoutResponse deleteToken(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        KakaoLogoutResponse logoutResponse = restTemplate.postForObject(
            kakaoApiProperties.getLogoutUrl(),
            requestEntity,
            KakaoLogoutResponse.class
        );

        return logoutResponse;
    }
}
