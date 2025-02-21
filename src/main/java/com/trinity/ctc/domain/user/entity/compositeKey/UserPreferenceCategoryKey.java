package com.trinity.ctc.domain.user.entity.compositeKey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class UserPreferenceCategoryKey implements Serializable {

    @Column(name = "user_preference_id")
    Long userPreferenceId;

    @Column(name = "category_id")
    Long categoryId;
}
