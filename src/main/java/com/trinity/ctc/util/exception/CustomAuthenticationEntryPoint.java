package com.trinity.ctc.util.exception;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        log.warn("🚨 Authentication error: {}", authException.getMessage());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (authException.getMessage().contains("온보딩이 필요한 임시 사용자입니다")) {
            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED); // 412
            response.getWriter().write("{\"error\": \"NEED_ONBOARDING\", \"message\": \"" + authException.getMessage() + "\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            response.getWriter().write("{\"error\": \"UNAUTHORIZED\", \"message\": \"Full authentication is required to access this resource\"}");
        }
    }
}
