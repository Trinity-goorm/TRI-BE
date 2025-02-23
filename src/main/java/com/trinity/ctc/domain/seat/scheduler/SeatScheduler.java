
package com.trinity.ctc.domain.seat.scheduler;

import com.trinity.ctc.domain.seat.service.SeatBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ShellComponent
public class SeatScheduler {

    private final SeatBatchService seatBatchService;

    /**
     * 매월 1일 04:00 am 다음달 한달치 예약정보 삽입
     */
    @Scheduled(cron = "0 0 4 1 * ?")
    public void seatInsertion() {
        System.out.println("=== Seat 데이터 생성 시작 ===");
        seatBatchService.batchInsertSeatProd();
        System.out.println("=== Seat 데이터 생성 완료 ===");
    }
}