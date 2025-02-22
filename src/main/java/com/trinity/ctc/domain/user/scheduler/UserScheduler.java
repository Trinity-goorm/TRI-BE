package com.trinity.ctc.domain.user.scheduler;

import com.trinity.ctc.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserScheduler {
    private final UserService userService;

    /**
     * 매월 1일 00:00에 모든 유저의 빈자리 티켓 개수를 10개로 초기화
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void resetEmptyTicket() {
        userService.resetEmptyTicket();
    }
}
