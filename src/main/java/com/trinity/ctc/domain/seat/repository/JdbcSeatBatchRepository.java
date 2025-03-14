package com.trinity.ctc.domain.seat.repository;

import com.trinity.ctc.domain.seat.entity.Seat;
import com.trinity.ctc.global.monitoring.HikariCPMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcSeatBatchRepository implements SeatBatchRepository {

    private final JdbcTemplate jdbcTemplate;
    private final HikariCPMonitor hikariCPMonitor;

    private static final String SQL_INSERT_SEAT = """
    INSERT INTO seat (reservation_date, available_seats, restaurant_id, reservation_time_id, seat_type_id)
    VALUES (?, ?, ?, ?, ?)
""";

    @Override
    @Transactional
    public void batchInsertSeats(List<Seat> seats, int batchSize) {
        int totalSize = seats.size();
        log.info("🚀 [JDBC] INSERT 할 Seat 데이터 개수: {}", totalSize);

        try {
            int count = 0;
            for (int i = 0; i < totalSize; i += batchSize) {
                List<Seat> batchList = seats.subList(i, Math.min(i + batchSize, totalSize));

                jdbcTemplate.batchUpdate(SQL_INSERT_SEAT,
                        batchList,
                        batchSize,
                        (ps, seat) -> {
                            ps.setDate(1, java.sql.Date.valueOf(seat.getReservationDate()));
                            ps.setInt(2, seat.getAvailableSeats());
                            ps.setLong(3, seat.getRestaurant().getId());
                            ps.setLong(4, seat.getReservationTime().getId());
                            ps.setLong(5, seat.getSeatType().getId());
                        }
                );

                count += batchList.size();
                log.info("✅ 현재까지 INSERT된 Seat 개수: {}/{}", count, totalSize);
            }
            log.info("🚀 모든 배치 INSERT 완료! 총 {}개 데이터 삽입됨.", totalSize);
        } catch (Exception e) {
            log.error("❌ 배치 삽입 중 오류 발생: {}", e.getMessage(), e);
            throw e;  // 예외 발생 시 스택 트레이스 출력
        }
    }


}
