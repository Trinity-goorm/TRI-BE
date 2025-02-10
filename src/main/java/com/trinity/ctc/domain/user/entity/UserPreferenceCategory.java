package com.trinity.ctc.domain.user.entity;

import com.trinity.ctc.domain.category.entity.Category;
import jakarta.persistence.*;

@Entity
public class UserPreferenceCategory {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_preference_id")
    private UserPreference userPreference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category")
    private Category category;
}

