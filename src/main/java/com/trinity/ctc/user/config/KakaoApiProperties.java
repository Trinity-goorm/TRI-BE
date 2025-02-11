package com.trinity.ctc.user.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kakao.api")
@Getter
public class KakaoApiProperties {

    private String tokenUrl;
    private String clientId;
    private String redirectUri;
    private String userInfoUrl;
    private String grantType;
}
