package com.trinity.ctc.global.kakao.controller;

import com.trinity.ctc.domain.fcm.service.FcmService;
import com.trinity.ctc.global.kakao.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/users/kakao")
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;
    private final FcmService fcmService;


//    @PostMapping("/login")
//    @Operation(
//        summary = "카카오 로그인",
//        description = "카카오 로그인을 수행합니다."
//    )
//    @ApiResponse(
//            responseCode = "200",
//            description = "카카오 로그인 성공",
//            content = @Content(
//                    mediaType = "application/json",
//                    schema = @Schema(implementation = UserLoginResponse.class)
//            )
//    )
//    public ResponseEntity<UserLoginResponse> kakaoLogin(@RequestParam String code, @RequestBody FcmTokenRequest fcmTokenRequest) {
//        UserLoginResponse response = authService.authenticateWithKakao(code);
//
//        fcmService.registerFcmToken(fcmTokenRequest, response.getId());
//        return ResponseEntity.ok(response);
//    }
//
//    @PostMapping("/logout")
//    @Operation(
//        summary = "카카오 로그아웃",
//        description = "카카오 로그아웃을 수행합니다."
//    )
//    @ApiResponse(
//            responseCode = "200",
//            description = "카카오 로그아웃 성공",
//            content = @Content(
//                    mediaType = "application/json",
//                    schema = @Schema(implementation = KakaoLogoutResponse.class)
//            )
//    )
//    public ResponseEntity<KakaoLogoutResponse> kakaoLogout(@RequestHeader("Authorization") String authorizationHeader, @RequestBody FcmTokenRequest fcmTokenRequest) {
//        String accessToken = authorizationHeader.replace("Bearer ", "").trim();
//        KakaoLogoutResponse logoutResponse = authService.logout(accessToken);
//
//        fcmService.deleteFcmToken(fcmTokenRequest);
//
//        return ResponseEntity.ok(logoutResponse);
//    }
}
