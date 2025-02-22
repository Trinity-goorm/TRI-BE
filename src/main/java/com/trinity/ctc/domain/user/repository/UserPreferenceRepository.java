package com.trinity.ctc.domain.user.repository;

import com.trinity.ctc.domain.user.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    UserPreference findByUserId(Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.emptyTicketCount = 10")
    void resetAllEmptyTickets();
}
