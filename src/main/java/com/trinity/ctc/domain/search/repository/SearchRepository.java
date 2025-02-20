package com.trinity.ctc.domain.search.repository;

import com.trinity.ctc.domain.search.entity.SearchHistory;
import com.trinity.ctc.domain.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchRepository extends JpaRepository<SearchHistory, Long> {
    @Query("SELECT s.keyword FROM SearchHistory s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<String> findTopByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    Optional<SearchHistory> findByKeywordAndUser(String keyword, User user);
}
