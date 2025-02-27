package com.trinity.ctc.domain.user.repository;

import com.trinity.ctc.domain.user.entity.User;
import java.util.Optional;

import com.trinity.ctc.kakao.dto.KakaoUserInfoResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKakaoId(Long kakaoId);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.emptyTicketCount = 10")
    void resetAllEmptyTickets();
}
