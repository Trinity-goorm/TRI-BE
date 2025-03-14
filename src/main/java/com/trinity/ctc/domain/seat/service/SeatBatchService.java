package com.trinity.ctc.domain.seat.service;

import com.trinity.ctc.domain.reservation.entity.ReservationTime;
import com.trinity.ctc.domain.reservation.service.ReservationTimeService;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.service.RestaurantService;
import com.trinity.ctc.domain.seat.entity.Seat;
import com.trinity.ctc.domain.seat.entity.SeatType;
import com.trinity.ctc.domain.seat.mode.DateRangeMode;
import com.trinity.ctc.domain.seat.repository.SeatBatchRepository;
import com.trinity.ctc.domain.seat.repository.SeatRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SeatBatchService {

    private final SeatPreparationService seatPreparationService;
    private final SeatBatchRepository seatBatchRepository;

    public SeatBatchService(SeatPreparationService seatPreparationService, @Qualifier("jdbcSeatBatchRepository") SeatBatchRepository seatBatchRepository) {
        this.seatPreparationService = seatPreparationService;
        this.seatBatchRepository = seatBatchRepository;
    }

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size:500}")
    private int batchSize;
    @Value("${common.seat.available.count:1}")
    private int availableSeatCount;

    @Transactional
    public void batchInsertSeatProd() {
        executeBatchInsert(DateRangeMode.NEXT_MONTH);
    }

    @Transactional
    public void batchInsertSeatDummy() {
        executeBatchInsert(DateRangeMode.TWO_MONTHS);
    }

    private void executeBatchInsert(DateRangeMode mode) {
        Instant start = Instant.now();

        List<Seat> seats = seatPreparationService.prepareSeatsData(mode, availableSeatCount);
        log.info("🚀 INSERT 할 Seat 데이터 개수: {}", seats.size());

        seatBatchRepository.batchInsertSeats(seats, batchSize);

        Instant end = Instant.now();
        long elapsedTimeMs = Duration.between(start, end).toMillis();
        double throughput = ((double) seats.size() / ((double) elapsedTimeMs / 1000));

        log.info("✅ 배치 INSERT 완료! 실행 시간: {} ms, 초당 처리량: {}", elapsedTimeMs, throughput);
    }
}