package com.trinity.ctc.domain.seat.service;

import com.trinity.ctc.domain.reservation.entity.ReservationTime;
import com.trinity.ctc.domain.reservation.service.ReservationTimeService;
import com.trinity.ctc.domain.restaurant.entity.Restaurant;
import com.trinity.ctc.domain.restaurant.service.RestaurantService;
import com.trinity.ctc.domain.seat.entity.SeatAvailability;
import com.trinity.ctc.domain.seat.entity.SeatType;
import com.trinity.ctc.domain.seat.mode.DateRangeMode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatAvailabilityBatchService {

    @PersistenceContext
    private EntityManager entityManager;

    private final RestaurantService restaurantService;
    private final ReservationTimeService reservationTimeService;
    private final SeatTypeService seatTypeService;

    @Value("${batch.insert.size:1000}")
    private int batchSize;
    @Value("${common.seat.available.count:1}")
    private int availableSeatCount;

    @Transactional
    public void batchInsertSeatAvailabilityProd() {
        long startTime = System.nanoTime();

        List<SeatAvailability> seatAvailabilities = prepareSeatAvailabilityData(DateRangeMode.NEXT_MONTH);

        int totalSize = seatAvailabilities.size();
        log.info("🚀 INSERT 할 SeatAvailability 데이터 개수: {}", totalSize);

        for (int i = 0; i < totalSize; i++) {
            entityManager.persist(seatAvailabilities.get(i));

            if ((i + 1) % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.flush();
        entityManager.clear();

        long endTime = System.nanoTime();
        log.info("✅ 배치 INSERT 완료! 실행 시간: {}ms", (endTime - startTime) / 1_000_000);
    }

    @Transactional
    public void batchInsertSeatAvailabilityDummy() {
        long startTime = System.nanoTime();

        List<SeatAvailability> seatAvailabilities = prepareSeatAvailabilityData(DateRangeMode.TWO_MONTHS);

        int totalSize = seatAvailabilities.size();
        log.info("🚀 INSERT 할 SeatAvailability 데이터 개수: {}", totalSize);

        for (int i = 0; i < totalSize; i++) {
            entityManager.persist(seatAvailabilities.get(i));

            if ((i + 1) % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.flush();
        entityManager.clear();

        long endTime = System.nanoTime();
        log.info("✅ 배치 INSERT 완료! 실행 시간: {}ms", (endTime - startTime) / 1_000_000);
    }

    private List<SeatAvailability> prepareSeatAvailabilityData(DateRangeMode mode) {
        List<Restaurant> restaurants = restaurantService.getAllRestaurants();
        List<ReservationTime> reservationTimes = reservationTimeService.getAllReservationTimes();
        List<SeatType> seatTypes = seatTypeService.getAllSeatTypes();

        LocalDate startDate;
        LocalDate endDate;

        if (mode == DateRangeMode.NEXT_MONTH) {
            startDate = LocalDate.now().withDayOfMonth(1).plusMonths(1); // 다음 달 1일
            endDate = startDate.plusMonths(1); // 다음 달 말일까지
        } else {
            startDate = LocalDate.now().withDayOfMonth(1); // 이번 달 1일
            endDate = startDate.plusMonths(2); // 다음 달 말일까지
        }

        List<SeatAvailability> seatAvailabilities = new ArrayList<>();

        for (LocalDate reservationDate = startDate; reservationDate.isBefore(endDate); reservationDate = reservationDate.plusDays(1)) {
            for (Restaurant restaurant : restaurants) {
                for (ReservationTime reservationTime : reservationTimes) {
                    for (SeatType seatType : seatTypes) {
                        seatAvailabilities.add(SeatAvailability.create(restaurant, reservationDate, reservationTime, seatType, availableSeatCount));
                    }
                }
            }
        }

        return seatAvailabilities;
    }
}