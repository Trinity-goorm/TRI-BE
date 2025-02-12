package com.trinity.ctc;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // POST 테스트 시 CSRF 비활성화
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // CORS 설정
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("users/kakao/login").permitAll()  // 인증 없이 접근 허용
                .requestMatchers("users/kakao/logout").permitAll()  // 인증 없이 접근 허용
                .anyRequest().authenticated()  // 그 외 경로는 인증 필요
            )  // 기본 로그인 페이지 비활성화
            .httpBasic(basic->basic.disable())
            .formLogin(form -> form.disable());  // HTTP Basic 인증 비활성화

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));  // 허용할 프론트엔드 도메인
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));  // 허용할 HTTP 메서드
        configuration.setAllowedHeaders(List.of("*"));  // 모든 요청 헤더 허용
        configuration.setAllowCredentials(true);  // 쿠키나 인증 정보를 허용할 경우 true

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // 모든 경로에 CORS 설정 적용
        return source;
    }
}
