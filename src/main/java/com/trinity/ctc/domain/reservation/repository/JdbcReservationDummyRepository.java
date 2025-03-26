package com.trinity.ctc.domain.reservation.repository;

import com.trinity.ctc.domain.reservation.entity.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcReservationDummyRepository implements ReservationDummyRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void batchInsertReservations(List<Reservation> reservations, int batchSize) {
        String sql = "INSERT INTO reservation (reservation_date, status, restaurant_id, user_id, reservation_time_id, seat_type_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        log.info("✅ Reservation Insert 시작 - KeyHolder로 PK 추출");

        for (Reservation reservation : reservations) {
            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
                ps.setObject(1, reservation.getReservationDate());
                ps.setString(2, reservation.getStatus().name());
                ps.setLong(3, reservation.getRestaurant().getId());
                ps.setLong(4, reservation.getUser().getId());
                ps.setLong(5, reservation.getReservationTime().getId());
                ps.setLong(6, reservation.getSeatType().getId());

                return ps;
            });
        }

        log.info("✅ Reservation Insert 완료");
    }
}
