package com.trinity.ctc.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trinity.ctc.global.exception.error_code.UserErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAccessDeniedHandler extends Throwable implements AccessDeniedHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        log.warn("🚨 Access Denied: {}", accessDeniedException.getMessage());

        if (accessDeniedException.getMessage().contains("온보딩이 필요한 임시 사용자")) {
            response.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED); // 412 반환
            response.setContentType("application/json");
            response.getWriter().write(mapper.writeValueAsString(UserErrorCode.NEED_ON_BOARDING));
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
        }
    }
}
