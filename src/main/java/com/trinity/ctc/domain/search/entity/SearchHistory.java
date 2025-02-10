package com.trinity.ctc.domain.search.entity;

import com.trinity.ctc.domain.user.entity.User;
import jakarta.persistence.*;

@Entity
public class SearchHistory {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
