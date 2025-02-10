package com.trinity.ctc.domain.user.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class UserPreference {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int min_price;
    private int max_price;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "userPreference")
    private List<UserPreferenceCategory> userPReferenceCategoryList = new ArrayList<>();
}
