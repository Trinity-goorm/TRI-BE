package com.trinity.ctc.domain.user.entity;

import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.user.entity.compositeKey.UserPreferenceCategoryKey;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreferenceCategory {

    @EmbeddedId
    UserPreferenceCategoryKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userPreferenceId")
    @JoinColumn(name = "user_preference_id")
    private UserPreference userPreference;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId")
    @JoinColumn(name = "category_id")
    private Category category;

    public static UserPreferenceCategory of(UserPreference userPreference, Category category) {

        UserPreferenceCategory userPreferenceCategory = new UserPreferenceCategory();
        userPreferenceCategory.id = new UserPreferenceCategoryKey(userPreference.getId(), category.getId());
        userPreferenceCategory.userPreference = userPreference;
        userPreferenceCategory.category = category;
        return userPreferenceCategory;
    }
}

