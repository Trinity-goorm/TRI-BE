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
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int minPrice;
    private int maxPrice;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "userPreference")
    private List<UserPreferenceCategory> userPreferenceCategoryList = new ArrayList<>();

    @Builder
    public UserPreference(int minPrice, int maxPrice, List<UserPreferenceCategory> userPreferenceCategoryList, User user) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.userPreferenceCategoryList = userPreferenceCategoryList;
        this.user = user;
    }
}
