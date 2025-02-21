package com.trinity.ctc.domain.seat.service;

import com.trinity.ctc.domain.seat.entity.SeatType;
import com.trinity.ctc.domain.seat.repository.SeatTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatTypeService {

    private final SeatTypeRepository seatTypeRepository;

    @Transactional(readOnly = true)
    public List<SeatType> getAllSeatTypes() {
        log.debug("[SELECT] 모든 좌석타입 획득");
        return seatTypeRepository.findAll();
    }
}
