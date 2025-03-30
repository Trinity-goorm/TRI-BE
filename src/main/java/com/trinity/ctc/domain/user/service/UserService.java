package com.trinity.ctc.domain.user.service;

import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.category.repository.CategoryRepository;
import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.repository.ReservationRepository;
import com.trinity.ctc.domain.restaurant.dto.RestaurantCategoryName;
import com.trinity.ctc.domain.restaurant.entity.RestaurantCategory;
import com.trinity.ctc.domain.restaurant.repository.RestaurantCategoryRepository;
import com.trinity.ctc.domain.user.dto.OnboardingRequest;
import com.trinity.ctc.domain.user.dto.ReissueTokenRequest;
import com.trinity.ctc.domain.user.dto.UserDetailResponse;
import com.trinity.ctc.domain.user.dto.UserReservationListResponse;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.entity.UserPreference;
import com.trinity.ctc.domain.user.entity.UserPreferenceCategory;
import com.trinity.ctc.domain.user.repository.UserRepository;
import com.trinity.ctc.domain.user.validator.UserValidator;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.UserErrorCode;
import com.trinity.ctc.global.kakao.service.AuthService;
import com.trinity.ctc.global.util.common.SortOrder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.stream.Collectors;
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
    private final UserValidator userValidator;
    private final ReservationRepository reservationRepository;
    private final AuthService authService;
    private final TokenService tokenService;
    private final RestaurantCategoryRepository restaurantCategoryRepository;

    /**
     * 온보딩 요청 DTO의 정보로 user entity를 build 후 저장하는 메서드
     *
     * @param onboardingRequest
     */
    @Transactional

    public void saveOnboardingInformation(OnboardingRequest onboardingRequest, HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = request.getHeader("refresh");

        ReissueTokenRequest reissueTokenRequest = new ReissueTokenRequest(refreshToken);

        String kakaoId = authService.getAuthenticatedKakaoId();

        // update 할 사용자 entity select
        User user = userRepository.findByKakaoId(Long.valueOf(kakaoId))
                .orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));

        // 사용자가 온보딩 중인 사용자인지(status = TEMPORARILY_UNAVAILABLE) 검증, 아닐 경우 403 반환
        userValidator.validateUserStatus(user);
        // 사용자가 선호 카테고리를 3개 선택했는지 검증
        userValidator.validateCategorySelection(onboardingRequest.getUserPreferenceCategoryIdList().size());

        // 사용자가 선택한 선호 카테고리를 카테고리 table에서 select
        List<Category> categoryList = categoryRepository.findAllById(onboardingRequest.getUserPreferenceCategoryIdList());

        // userPreference entity build & save
        UserPreference userPreference = UserPreference.builder()
                .minPrice(onboardingRequest.getMinPrice())
                .maxPrice(onboardingRequest.getMaxPrice())
                .userPreferenceCategoryList(new ArrayList<>())
                .user(user)
                .build();

        // userPreference와 사용자가 선택한 category를 mapping하여 userPreferenceCategoryList build하고 add
        List<UserPreferenceCategory> userPreferenceCategoryList = categoryList.stream()
                .map(category -> UserPreferenceCategory.of(userPreference, category))
                .toList();
        userPreference.getUserPreferenceCategoryList().addAll(userPreferenceCategoryList);

        // user entity 내 update 메서드로 user와 영속화된 entity 모두 DB에 반영
        user.updateOnboardingInformation(onboardingRequest, userPreference);

        tokenService.reissueToken(reissueTokenRequest, request, response);
    }

    /**
     * 사용자 프로필 정보 반환
     * @return 사용자 프로필 정보
     */
    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail() {
        Long kakaoId = Long.parseLong(authService.getAuthenticatedKakaoId());
        User user = userRepository.findByKakaoId(kakaoId).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));
        return UserDetailResponse.of(user.getId(), user.getNickname(), user.getPhoneNumber(), user.getNormalTicketCount(), user.getEmptyTicketCount());
    }


    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetailV2(long kakaoId) {
        User user = userRepository.findByKakaoId(kakaoId).orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND));
        return UserDetailResponse.of(user.getId(), user.getNickname(), user.getPhoneNumber(), user.getNormalTicketCount(), user.getEmptyTicketCount());
    }


    /**
     * 사용자 예약리스트 반환
     * @return 예약정보 리스트 및 개수
     */
    @Transactional(readOnly = true)
    public UserReservationListResponse getUserReservations(int page, int size, String sortBy) {
        SortOrder sortOrder = SortOrder.fromString(sortBy);
        PageRequest pageRequest = PageRequest.of(page - 1, size, sortOrder.getSort());

        Long kakaoId = Long.parseLong(authService.getAuthenticatedKakaoId());

        Slice<Reservation> reservations = reservationRepository.findAllByKakaoId(kakaoId, pageRequest);

        List<Long> restaurantIds = reservations.getContent().stream()
            .map(res -> res.getRestaurant().getId())
            .distinct()
            .toList();

        List<RestaurantCategoryName> rcList = restaurantCategoryRepository
            .findAllWithCategoryByRestaurantIds(restaurantIds);

        return UserReservationListResponse.from(reservations, rcList);
    }

    /**
     * 모든 유저의 빈자리 티켓 개수를 10개로 초기화
     */
    public void resetEmptyTicket() {
        userRepository.resetAllEmptyTickets();
    }
}

