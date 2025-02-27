package com.trinity.ctc.domain.user.repository;

import com.trinity.ctc.domain.user.entity.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Boolean existsByRefreshToken(String refreshToken);

    @Transactional
    void deleteByRefreshToken(String refreshToken);

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiration < ?1")
    List<RefreshToken> findByExpirationBefore(String deadline);
}
