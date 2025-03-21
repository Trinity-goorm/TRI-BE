package com.trinity.ctc.domain.user.controller;

import com.trinity.ctc.domain.user.dto.OnboardingRequest;
import com.trinity.ctc.domain.user.dto.UserDetailResponse;
import com.trinity.ctc.domain.user.dto.UserReservationListResponse;
import com.trinity.ctc.domain.user.jwt.JWTUtil;
import com.trinity.ctc.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관련 API")
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping("/onboarding")
    @Operation(
            summary = "온보딩 정보 저장",
            description = "요청 시, user table에 해당 사용자의 온보딩 정보 저장"
    )
    @ApiResponse(
            responseCode = "204",
            description = "성공"
    )
    @ApiResponse(
            responseCode = "400",
            description = "사용자가 선호 카테고리를 3개 미만으로 선택 시, 400 반환"
    )
    @ApiResponse(
            responseCode = "403",
            description = "사용자가 임시 회원이 아닐 경우, 403 반환"
    )
    public ResponseEntity<Void> saveOnboardingInformation(@RequestBody OnboardingRequest onboardingRequest, HttpServletRequest request, HttpServletResponse response) {
        userService.saveOnboardingInformation(onboardingRequest, request, response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/detail")
    @Operation(
            summary = "사용자 프로필 정보 반환",
            description = "사용자 프로필 정보를 반환하는 API"
    )
    @ApiResponse(
            responseCode = "200",
            description = "성공"
    )
    public ResponseEntity<UserDetailResponse> getUserDetail() {
        UserDetailResponse result = userService.getUserDetail();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/reservations")
    @Operation(
            summary = "사용자 예약리스트 반환",
            description = "사용자 예약리스트를 반환하는 API"
    )
    @ApiResponse(
            responseCode = "200",
            description = "성공"
    )
    public ResponseEntity<UserReservationListResponse> getUserReservations(@RequestParam(defaultValue = "1") int page,
                                                                           @RequestParam(defaultValue = "10") int size,
                                                                           @Parameter(
                                                                                   description = "정렬 기준 (가능한 값: RESERVE_DATE_ASC, RESERVE_DATE_DESC)",
                                                                                   example = "RESERVE_DATE_DESC",
                                                                                   schema = @Schema(allowableValues = {
                                                                                           "RESERVE_DATE_ASC", "RESERVE_DATE_DESC"
                                                                                   })
                                                                           )
                                                                           @RequestParam(defaultValue = "RESERVE_DATE_DESC") String sortBy) {
        UserReservationListResponse userReservationListResponse = userService.getUserReservations(page, size, sortBy);
        return ResponseEntity.ok(userReservationListResponse);
    }

    @GetMapping("/detail/v2")
    @Operation(
            summary = "kakao ID 기반 사용자 프로필 정보 반환",
            description = "사용자 프로필 정보를 반환하는 API"
    )
    @ApiResponse(
            responseCode = "200",
            description = "성공"
    )
    public ResponseEntity<UserDetailResponse> getUserDetailV2(@RequestParam long kakaoId) {
        UserDetailResponse result = userService.getUserDetailV2(kakaoId);
        return ResponseEntity.ok(result);
    }
}
