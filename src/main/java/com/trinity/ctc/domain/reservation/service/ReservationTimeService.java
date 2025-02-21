package com.trinity.ctc.domain.reservation.service;

import com.trinity.ctc.domain.reservation.entity.ReservationTime;
import com.trinity.ctc.domain.reservation.repository.ReservationTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;

    @Transactional(readOnly = true)
    public List<ReservationTime> getAllReservationTimes() {
        log.debug("[SELECT] 모든 예약시간 획득");
        return reservationTimeRepository.findAll();
    }
}
