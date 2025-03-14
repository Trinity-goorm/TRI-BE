package com.trinity.ctc.domain.user.controller;


import com.trinity.ctc.domain.user.dto.ReissueTokenRequest;
import com.trinity.ctc.domain.user.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/reissue")
    public ResponseEntity<?> reissueToken(@RequestBody ReissueTokenRequest requestDto, HttpServletRequest request, HttpServletResponse response) {

        String newAccessToken = tokenService.reissueToken(requestDto, request, response);
        return ResponseEntity.ok("토큰 재발급 성공");
    }
}
