
package com.trinity.ctc.domain.seat.scheduler;

import com.trinity.ctc.domain.seat.service.SeatAvailabilityBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ShellComponent
public class SeatAvailabilityScheduler {

    private final SeatAvailabilityBatchService seatAvailabilityBatchService;

    /**
     * 매월 1일 04:00 am 다음달 한달치 예약정보 삽입
     */
    @Scheduled(cron = "0 0 4 1 * ?")
    public void seatAvailabilityInsertion() {
        System.out.println("=== SeatAvailability 데이터 생성 시작 ===");
        seatAvailabilityBatchService.batchInsertSeatAvailabilityProd();
        System.out.println("=== SeatAvailability 데이터 생성 완료 ===");
    }
}