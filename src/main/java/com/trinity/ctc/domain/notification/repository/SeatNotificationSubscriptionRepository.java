package com.trinity.ctc.domain.notification.repository;

import com.trinity.ctc.domain.notification.entity.SeatNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotificationSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatNotificationSubscriptionRepository extends JpaRepository<SeatNotificationSubscription, Long> {

    Optional<SeatNotificationSubscription> findByUserIdAndSeatNotification(Long userId, SeatNotification seatNotification);

    List<SeatNotificationSubscription> findAllByUserId(Long userId);

    int countBySeatNotification(SeatNotification seatNotification);

    @Query("SELECT s FROM SeatNotificationSubscription s join fetch s.user u join fetch s.user.fcmList f left join fetch s.user.userPreference uf WHERE s.seatNotification = :seatNotification")
    List<SeatNotificationSubscription> findAllBySeatNotification(@Param("seatNotification") SeatNotification seatNotification);
}
