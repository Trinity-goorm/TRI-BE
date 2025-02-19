package com.trinity.ctc.domain.notification.repository;

import com.trinity.ctc.domain.notification.entity.SeatNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotificationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatNotificationRepository extends JpaRepository<SeatNotification, Long> {

    @Query("Select s FROM SeatNotification s WHERE s.user.id = :userId")
    Optional<SeatNotification> findByUserId(@Param("userId") long userId);
}
