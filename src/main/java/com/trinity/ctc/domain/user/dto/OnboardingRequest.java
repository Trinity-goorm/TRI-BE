package com.trinity.ctc.domain.user.dto;

import com.trinity.ctc.domain.user.status.Sex;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Schema(description = "사용자 온보딩 정보")
public class OnboardingRequest {
    @Schema(description = "유저 ID", example = "1")
    private long userId;

    @Schema(description = "성별", example = "MALE")
    private Sex sex;

    @Schema(description = "닉네임", example = "USER1")
    private String name;

    @Schema(description = "생년월일", example = "1995-08-25")
    private LocalDate birthday;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "사용자 선호 최저 가격", example = "10000")
    private int minPrice;

    @Schema(description = "사용자 선호 최대 가격", example = "250000")
    private int maxPrice;

    @Schema(description = "사용자 선호 카테고리 id 리스트", example = "[1, 2, 3, 5]")
    private List<Long> userPreferenceCategoryIdList;
}
