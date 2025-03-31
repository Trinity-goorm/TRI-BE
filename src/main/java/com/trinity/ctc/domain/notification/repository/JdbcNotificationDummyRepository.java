package com.trinity.ctc.domain.notification.repository;

import com.google.common.collect.Lists;
import com.trinity.ctc.domain.notification.entity.ReservationNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotification;
import com.trinity.ctc.domain.notification.entity.SeatNotificationSubscription;
import com.trinity.ctc.domain.notification.type.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcNotificationDummyRepository implements NotificationDummyRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void batchInsertReservationNotifications(List<ReservationNotification> reservationNotifications, int batchSize) {
        String sql = "INSERT INTO reservation_notification (type, title, body, url, scheduled_time, user_id, reservation_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        log.info("✅ ReservationNotification Insert 시작");

        List<List<ReservationNotification>> batches = Lists.partition(reservationNotifications, batchSize);

        for(List<ReservationNotification> batch : batches) {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ReservationNotification reservationNotification = batch.get(i);
                    ps.setString(1, reservationNotification.getType().name());
                    ps.setString(2, reservationNotification.getTitle());
                    ps.setString(3, reservationNotification.getBody());
                    ps.setString(4, reservationNotification.getUrl());
                    ps.setObject(5, reservationNotification.getScheduledTime());
                    ps.setLong(6, reservationNotification.getUser().getId());
                    ps.setLong(7, reservationNotification.getReservation().getId());
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }

        log.info("✅ ReservationNotification Insert 완료");
    }

    @Override
    public void batchInsertSeatNotifications(List<SeatNotification> seatNotifications, int batchSize) {
        String sql = "INSERT INTO seat_notification (title, body, url, seat_id) VALUES (?, ?, ?, ?)";

        log.info("✅ SeatNotification Insert 시작");

        List<List<SeatNotification>> batches = Lists.partition(seatNotifications, batchSize);

        for(List<SeatNotification> batch : batches) {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    SeatNotification seatNotification = batch.get(i);
                    ps.setString(1, seatNotification.getTitle());
                    ps.setString(2, seatNotification.getBody());
                    ps.setString(3, seatNotification.getUrl());
                    ps.setLong(4, seatNotification.getSeat().getId());
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }

        log.info("✅ SeatNotification Insert 완료 (총 {}건)", seatNotifications.size());
    }


    @Override
    public void batchInsertSeatNotificationSubscriptions(List<SeatNotificationSubscription> subscriptions, int batchSize) {
        String sql = "INSERT INTO seat_notification_subscription (user_id, seat_notification_id) VALUES (?, ?)";

        log.info("✅ SeatNotificationSubscription Insert 시작");

        List<List<SeatNotificationSubscription>> batches = Lists.partition(subscriptions, batchSize);

        for(List<SeatNotificationSubscription> batch : batches) {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    SeatNotificationSubscription seatNotificationSubscription = batch.get(i);
                    ps.setLong(1, seatNotificationSubscription.getUser().getId());
                    ps.setLong(2, seatNotificationSubscription.getSeatNotification().getId());
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }

        log.info("✅ SeatNotificationSubscription Insert 완료 (총 {}건)", subscriptions.size());
    }

}
