package com.trinity.ctc.kakao.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kakao.api")
@Getter
@Setter
public class KakaoApiProperties {

    private String tokenUrl;
    private String clientId;
    private String redirectUri;
    private String userInfoUrl;
    private String grantType;
}
