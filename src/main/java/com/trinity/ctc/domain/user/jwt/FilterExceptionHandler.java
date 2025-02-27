package com.trinity.ctc.domain.user.jwt;

import com.trinity.ctc.util.exception.CustomAccessDeniedException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class FilterExceptionHandler implements Filter {
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public FilterExceptionHandler(AuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (CustomAccessDeniedException ex) {
            log.warn("🚨 CustomAccessDeniedException caught: {}", ex.getMessage());

            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;

            authenticationEntryPoint.commence(request, response, new AuthenticationException(ex.getMessage()) {});
        } catch (Exception ex) {
            log.error("🚨 Unhandled exception: {}", ex.getMessage(), ex);
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error occurred.");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
