package com.trinity.ctc.domain.fcm.repository;

import com.trinity.ctc.domain.fcm.entity.Fcm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FcmRepository extends JpaRepository<Fcm, Long> {
    @Transactional
    @Modifying
    @Query("DELETE FROM Fcm f WHERE f.token = :token")
    void deleteByToken(@Param("token") String token);

    @Transactional
    @Modifying
    @Query("DELETE FROM Fcm f WHERE f.expiresAt < :currentDate")
    void deleteByExpiresAtBefore(LocalDateTime currentDate);

    @Transactional
    @Modifying
    @Query("UPDATE Fcm f SET f.updatedAt = :updatedAt, f.expiresAt = :expiresAt WHERE f.token = :token")
    void updateToken(@Param("token") String token,
                     @Param("updatedAt") LocalDateTime updatedAt,
                     @Param("expiresAt") LocalDateTime expiresAt);

    @Query("SELECT f.token FROM Fcm f WHERE f.user.id = :userId")
    List<String> findByUser(@Param("userId") Long userId);
}
