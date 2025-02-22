package com.trinity.ctc.domain.user.controller;

import com.trinity.ctc.domain.user.dto.OnboardingRequest;
import com.trinity.ctc.domain.user.dto.UserDetailResponse;
import com.trinity.ctc.domain.user.dto.UserReservationListResponse;
import com.trinity.ctc.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    public ResponseEntity<Void> saveOnboardingInformation(@RequestBody OnboardingRequest onboardingRequest) {
        userService.saveOnboardingInformation(onboardingRequest);
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
    public ResponseEntity<UserDetailResponse> getUserDetail(@RequestParam long userId) {
        UserDetailResponse result = userService.getUserDetail(userId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/reservations/{userId}")
    @Operation(
            summary = "사용자 예약리스트 반환",
            description = "사용자 예약리스트를 반환하는 API"
    )
    @ApiResponse(
            responseCode = "200",
            description = "성공"
    )
    public ResponseEntity<UserReservationListResponse> getUserReservations(@PathVariable long userId) {
        UserReservationListResponse userReservationListResponse = userService.getUserReservations(userId);
        return ResponseEntity.ok(userReservationListResponse);
    }
}
