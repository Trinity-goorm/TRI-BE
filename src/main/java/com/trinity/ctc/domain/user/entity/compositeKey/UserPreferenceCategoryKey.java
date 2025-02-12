package com.trinity.ctc.domain.user.entity.compositeKey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class UserPreferenceCategoryKey implements Serializable {

    @Column(name = "user_preference_id")
    Long userPreferenceId;

    @Column(name = "category_id")
    Long categoryId;
}
