package com.trinity.ctc.domain.seat.scheduler;

import com.trinity.ctc.domain.seat.service.SeatAvailabilityBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeatAvailabilityScheduler {

    private final SeatAvailabilityBatchService seatAvailabilityBatchService;

    @Scheduled(cron = "0 21 17 * * ?")
    public void seatAvailabilityInsertion() {
        System.out.println("=== SeatAvailability 데이터 생성 시작 ===");
        seatAvailabilityBatchService.batchInsertSeatAvailabilityV1();
        System.out.println("=== SeatAvailability 데이터 생성 완료 ===");
    }
}
