package com.trinity.ctc.user.service;

import com.trinity.ctc.user.config.KakaoApiProperties;
import com.trinity.ctc.user.dto.KakaoTokenResponse;
import com.trinity.ctc.user.dto.KakaoUserInfoResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("grant_type", kakaoApiProperties.getGrantType());
        body.add("client_id", kakaoApiProperties.getClientId());
        body.add("redirect_uri", kakaoApiProperties.getRedirectUri());
        body.add("code", authorizationCode);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        KakaoTokenResponse response = restTemplate.postForObject(
                kakaoApiProperties.getTokenUrl(),
                requestEntity,
                KakaoTokenResponse.class
        );


        return response;


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
