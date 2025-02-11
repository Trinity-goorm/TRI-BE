package com.trinity.ctc.user.service;

import com.trinity.ctc.user.config.KakaoApiProperties;
import com.trinity.ctc.user.dto.KakaoTokenResponse;
import com.trinity.ctc.user.dto.KakaoUserInfoResponse;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class KakaoApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final KakaoApiProperties kakaoApiProperties;

    public KakaoApiService(KakaoApiProperties kakaoApiProperties) {
        this.kakaoApiProperties = kakaoApiProperties;
    }

    public KakaoTokenResponse getAccessToken(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, String> params = Map.of(
            "grant_type", kakaoApiProperties.getGrantType(),
            "client_id", kakaoApiProperties.getClientId(),
            "redirect_uri", kakaoApiProperties.getRedirectUri(),
            "code", authorizationCode
        );

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<KakaoTokenResponse> response = restTemplate.exchange(
            kakaoApiProperties.getTokenUrl(),
            HttpMethod.POST,
            requestEntity,
            KakaoTokenResponse.class
        );

        return response.getBody();
    }

    public KakaoUserInfoResponse getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserInfoResponse> response = restTemplate.exchange(
            kakaoApiProperties.getUserInfoUrl(),
            HttpMethod.GET,
            requestEntity,
            KakaoUserInfoResponse.class
        );

        return response.getBody();
    }
}
