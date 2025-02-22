package com.trinity.ctc.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPreference {

    @Id
    private long id; // user_id로 식별(식별 관계)

    private int minPrice;
    private int maxPrice;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "userPreference", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPreferenceCategory> userPreferenceCategoryList = new ArrayList<>();

    @Builder
    public UserPreference(int minPrice, int maxPrice, List<UserPreferenceCategory> userPreferenceCategoryList, User user) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.userPreferenceCategoryList = userPreferenceCategoryList;
        this.user = user;
    }
}
