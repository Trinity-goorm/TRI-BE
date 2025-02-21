package com.trinity.ctc.domain.user.dto;

import com.trinity.ctc.domain.user.status.Sex;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class OnboardingRequest {
    private long userId;
    private Sex sex;
    private LocalDate birthday;
    private String phoneNumber;
    private int minPrice;
    private int maxPrice;
    private List<Long> userPreferenceCategoryIdList;

//    사용자Id, 성별, 생년월일, 전화번호, 선호 가격대, 선호 카테고리
}
