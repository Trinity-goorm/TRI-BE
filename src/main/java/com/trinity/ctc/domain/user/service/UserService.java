package com.trinity.ctc.domain.user.service;

import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.category.repository.CategoryRepository;
import com.trinity.ctc.domain.user.dto.OnboardingRequest;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.entity.UserPreference;
import com.trinity.ctc.domain.user.entity.UserPreferenceCategory;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.UserErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 온보딩 요청 DTO의 정보로 user entity를 build 후 저장하는 메서드
     * @param onboardingRequest
     */
    public void saveOnboardingInformation(OnboardingRequest onboardingRequest) {
        List<Category> categoryList = categoryRepository.findAllById(onboardingRequest.getUserPreferenceCategoryIdList());

        if(categoryList.size() < 3) throw new CustomException(UserErrorCode.NOT_ENOUGH_CATEGORY_SELECT);

        UserPreference userPreference = UserPreference.builder()
                .minPrice(onboardingRequest.getMinPrice())
                .maxPrice(onboardingRequest.getMaxPrice())
                .userPreferenceCategoryList(new ArrayList<>())
                .build();

        log.info("userPreference: {}", userPreference);

        List<UserPreferenceCategory> userPreferenceCategoryList = categoryList.stream()
                .map(category -> UserPreferenceCategory.builder()
                        .userPreference(userPreference)
                        .category(category)
                        .build())
                .toList();

        userPreference.getUserPreferenceCategoryList().addAll(userPreferenceCategoryList);

        log.info("userPreferenceCategoryList: {}", userPreferenceCategoryList);
        log.info("userPreference: {}", userPreference);

        User user = User.builder()
                .id(onboardingRequest.getUserId())
                .sex(onboardingRequest.getSex())
                .birthday(onboardingRequest.getBirthday())
                .phoneNumber(onboardingRequest.getPhoneNumber())
                .userPreference(userPreference)
                .build();

        log.info("user: {}", user);

        userRepository.save(user);
    }
}
