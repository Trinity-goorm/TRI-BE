package com.trinity.ctc.domain.user.factory;

import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.category.repository.CategoryRepository;
import com.trinity.ctc.domain.user.entity.User;
import com.trinity.ctc.domain.user.entity.UserPreference;
import com.trinity.ctc.domain.user.entity.UserPreferenceCategory;
import com.trinity.ctc.domain.user.status.Sex;
import com.trinity.ctc.domain.user.status.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UserFactory {

    private final CategoryRepository categoryRepository;

    public List<User> createUsersByCsv(List<Map<String, String>> csvUserData) {
        List<User> users = new ArrayList<>();
        for (Map<String, String> row : csvUserData) {
            User user = User.builder()
                    .kakaoId(Long.parseLong(row.get("kakao_id")))
                    .nickname(row.get("nickname"))
                    .phoneNumber(row.get("phone_number"))
                    .normalTicketCount(Integer.parseInt(row.get("normal_ticket_count")))
                    .emptyTicket(Integer.parseInt(row.get("empty_ticket_count")))
                    .status(UserStatus.valueOf(row.get("status").toUpperCase()))
                    .sex(Sex.valueOf(row.get("sex").toUpperCase()))
                    .imageUrl(row.get("image_url"))
                    .birthday(LocalDate.parse(row.get("birthday")))
                    .isDeleted(Boolean.parseBoolean(row.getOrDefault("is_deleted", "false")))
                    .build();
            users.add(user);
        }
        return users;
    }

    public List<UserPreference> createPreferencesByCsv(List<Map<String, String>> csvPreferenceData, Map<String, User> userMap) {
        List<UserPreference> preferences = new ArrayList<>();
        for (Map<String, String> row : csvPreferenceData) {
            User user = userMap.get(row.get("nickname"));
            if (user != null) {
                UserPreference pref = UserPreference.builder()
                        .minPrice(Integer.parseInt(row.get("min_price")))
                        .maxPrice(Integer.parseInt(row.get("max_price")))
                        .user(user)
                        .build();
                preferences.add(pref);
            }
        }
        return preferences;
    }

    public List<UserPreferenceCategory> createPreferenceCategoriesByCsv(List<Map<String, String>> csvData, Map<Long, UserPreference> preferenceMap) {
        List<UserPreferenceCategory> categories = new ArrayList<>();
        for (Map<String, String> row : csvData) {
            Long preferenceId = Long.parseLong(row.get("user_preference_id"));
            Long categoryId = Long.parseLong(row.get("category_id"));
            UserPreference userPreference = preferenceMap.get(preferenceId);
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (userPreference != null && category != null) {
                categories.add(UserPreferenceCategory.of(userPreference, category));
            }
        }
        return categories;
    }
}
