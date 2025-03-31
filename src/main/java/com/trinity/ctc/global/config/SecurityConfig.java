package com.trinity.ctc.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinity.ctc.domain.user.jwt.*;
import com.trinity.ctc.domain.user.repository.RefreshTokenRepository;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.global.exception.CustomAccessDeniedHandler;
import com.trinity.ctc.global.kakao.service.AuthService;
import com.trinity.ctc.global.kakao.service.KakaoApiService;
import com.trinity.ctc.global.exception.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final KakaoApiService kakaoApiService;
    private final AuthService authService;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserRepository userRepository, AuthService authService, CustomAccessDeniedHandler customAccessDeniedHandler, FilterExceptionHandler filterExceptionHandler) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)  // POST 테스트 시 CSRF 비활성화
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))  // CORS 설정
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/login", "/api/token", "/api/token/reissue", "/users/kakao/login", "/api/fcmTokens/register", "/api/fcmTokens/delete", "/api/data/**", "trigger/notifications/**").permitAll()
                    .requestMatchers("/api/users/onboarding").hasRole("TEMPORARILY_UNAVAILABLE")
                    .requestMatchers("/api/**", "/api/logout", "/users/kakao/logout").hasRole("AVAILABLE")
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated()  // 그 외 경로는 인증 필요
            )  // 기본 로그인 페이지 비활성화
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable);  // HTTP Basic 인증 비활성화

        http
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);

        LoginFilter loginFilter = new LoginFilter(jwtUtil, objectMapper, refreshTokenRepository, userRepository, kakaoApiService, authService);
        loginFilter.setFilterProcessesUrl("/api/login");
        http
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        http
                .addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshTokenRepository, kakaoApiService), LogoutFilter.class);

        //세션 설정
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
                .addFilterBefore(filterExceptionHandler, LoginFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "https://localhost:8080", "https://catch-ping.com"));  // 허용할 프론트엔드 도메인
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));  // 허용할 HTTP 메서드
        configuration.setAllowedHeaders(List.of("*"));  // 모든 요청 헤더 허용
        configuration.setAllowCredentials(true);  // 쿠키나 인증 정보를 허용할 경우 true

        configuration.setExposedHeaders(List.of("Access", "Refresh"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // 모든 경로에 CORS 설정 적용
        return source;
    }
}
