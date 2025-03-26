package com.trinity.ctc.domain.notification.repository;

import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotificationSubscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcNotificationDummyRepository implements NotificationDummyRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void batchInsertReservationNotifications(List<ReservationNotification> reservationNotifications, int batchSize) {
        String sql = "INSERT INTO reservationNotification (type, title, body, url, scheduled_time, user_id, reservation_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        log.info("✅ ReservationNotification Insert 시작");

        for (ReservationNotification reservationNotification : reservationNotifications) {
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, reservationNotification.getType().name());
                ps.setString(2, reservationNotification.getTitle());
                ps.setString(3, reservationNotification.getBody());
                ps.setString(4, reservationNotification.getUrl());
                ps.setObject(5, reservationNotification.getScheduledTime());
                ps.setLong(6, reservationNotification.getUser().getId());
                ps.setLong(7, reservationNotification.getReservation().getId());

                return ps;
            });
        }

        log.info("✅ ReservationNotification Insert 완료");
    }

    @Override
    public void batchInsertSeatNotifications(List<SeatNotification> seatNotifications, int batchSize) {
        String sql = "INSERT INTO seatNotification (title, body, url, seat_id) VALUES (?, ?, ?, ?)";

        log.info("✅ seatNotification Insert 시작");

        for (SeatNotification seatNotification : seatNotifications) {
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, seatNotification.getTitle());
                ps.setString(2, seatNotification.getBody());
                ps.setString(3, seatNotification.getUrl());
                ps.setLong(4, seatNotification.getSeat().getId());

                return ps;
            });
        }

        log.info("✅ seatNotification Insert 완료");
    }

    @Override
    public void batchInsertSeatNotificationSubscriptions(List<SeatNotificationSubscription> seatNotificationSubscriptions, int batchSize) {
        String sql = "INSERT INTO seatNotificationSubscription (user_id, seat_notification_id) VALUES (?, ?)";

        log.info("✅ seatNotificationSubscription Insert 시작");

        for (SeatNotificationSubscription seatNotificationSubscription : seatNotificationSubscriptions) {
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, seatNotificationSubscription.getUser().getId());
                ps.setLong(2, seatNotificationSubscription.getId());

                return ps;
            });
        }

        log.info("✅ seatNotificationNotification Insert 완료");
    }
}
