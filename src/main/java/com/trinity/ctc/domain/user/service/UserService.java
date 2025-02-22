package com.trinity.ctc.domain.user.service;

import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.category.repository.CategoryRepository;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.repository.ReservationRepository;
import com.trinity.ctc.domain.user.dto.OnboardingRequest;
import com.trinity.ctc.domain.user.dto.UserDetailResponse;
import com.trinity.ctc.domain.user.dto.UserReservationListResponse;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.entity.UserPreference;
import com.trinity.ctc.domain.user.entity.UserPreferenceCategory;
import com.trinity.ctc.domain.user.repository.UserPreferenceCategoryRepository;
import com.trinity.ctc.domain.user.repository.UserPreferenceRepository;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.domain.user.validator.UserValidator;
import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.UserErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserPreferenceCategoryRepository userPreferenceCategoryRepository;
    private final UserValidator userValidator;
    private final ReservationRepository reservationRepository;

    /**
     * 온보딩 요청 DTO의 정보로 user entity를 build 후 저장하는 메서드
     *
     * @param onboardingRequest
     */
    @Transactional
    public void saveOnboardingInformation(OnboardingRequest onboardingRequest) {
        User user = userRepository.findById(onboardingRequest.getUserId())
                .orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        userValidator.validateUserStatus(user);

        List<Category> categoryList = categoryRepository.findAllById(onboardingRequest.getUserPreferenceCategoryIdList());

        if (categoryList.size() < 3) throw new CustomException(UserErrorCode.NOT_ENOUGH_CATEGORY_SELECT);

        UserPreference userPreference = UserPreference.builder()
                .minPrice(onboardingRequest.getMinPrice())
                .maxPrice(onboardingRequest.getMaxPrice())
                .userPreferenceCategoryList(new ArrayList<>())
                .user(user)
                .build();

        userPreferenceRepository.save(userPreference);

        log.info("userPreference: {}", userPreference.getId());

        List<UserPreferenceCategory> userPreferenceCategoryList = categoryList.stream()
                .map(category -> UserPreferenceCategory.of(userPreference, category))
                .toList();

        userPreference.getUserPreferenceCategoryList().addAll(userPreferenceCategoryList);
        userPreferenceCategoryRepository.saveAll(userPreferenceCategoryList);

        log.info("userPreferenceCategoryList: {}", userPreferenceCategoryList.get(0).getId());
        log.info("userPreference: {}", userPreference.getUserPreferenceCategoryList().get(0).getId());
        log.info("user: {}", user);

        user.updateOnboardingInformation(onboardingRequest, userPreference);

        userRepository.save(user);
    }

    /**
     * 사용자 프로필 정보 반환
     * @param userId
     * @return 사용자 프로필 정보
     */
    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));
        return UserDetailResponse.of(user.getId(), user.getNickname(), user.getNormalTicketCount(), user.getEmptyTicketCount());
    }


    /**
     * 사용자 예약리스트 반환
     * @param userId
     * @return 예약정보 리스트 및 개수
     */
    @Transactional(readOnly = true)
    public UserReservationListResponse getUserReservations(long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Slice<Reservation> reservations = reservationRepository.findAllByUserId(userId, pageRequest);
        return UserReservationListResponse.from(reservations);
    }
}

