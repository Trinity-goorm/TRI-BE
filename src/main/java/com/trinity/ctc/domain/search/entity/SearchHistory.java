package com.trinity.ctc.domain.search.entity;

import com.trinity.ctc.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Entity
@RequiredArgsConstructor
public class SearchHistory {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String keyword;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public SearchHistory(String keyword, LocalDateTime createdAt, User user) {
        this.keyword = keyword;
        this.createdAt = createdAt;
        this.user = user;
    }

    public void updateCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }
}
