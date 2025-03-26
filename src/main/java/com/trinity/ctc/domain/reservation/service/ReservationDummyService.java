package com.trinity.ctc.domain.reservation.service;

import com.trinity.ctc.domain.reservation.entity.Reservation;
import com.trinity.ctc.domain.reservation.factory.ReservationFactory;
import com.trinity.ctc.domain.reservation.repository.ReservationDummyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationDummyService {
    private final ReservationDummyRepository reservationDummyRepository;
    private final ReservationFactory reservationFactory;

    @Transactional
    public void generateDummyData(List<Map<String, String>> reservationCsv, int batchSize) {
        log.info("✅ [ReservationDummyService] Reservation CSV 데이터 파싱 및 생성 시작");
        List<Reservation> reservations = reservationFactory.createReservationsByCsv(reservationCsv);
        log.info("✅ [ReservationDummyService] 생성된 Reservation 개수: {}", reservations.size());

        log.info("✅ [ReservationDummyService] Reservation 배치 저장 시작");
        reservationDummyRepository.batchInsertReservations(reservations, batchSize);
        log.info("✅ [ReservationDummyService] Reservation 배치 저장 완료");
    }
}
