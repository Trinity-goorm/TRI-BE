package com.trinity.ctc.domain.user.entity;

import com.trinity.ctc.domain.category.entity.Category;
import com.trinity.ctc.domain.user.entity.compositeKey.UserPreferenceCategoryKey;
import jakarta.persistence.*;

@Entity
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
}

