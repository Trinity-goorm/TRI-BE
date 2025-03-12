package com.trinity.ctc.global.monitoring;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public class HikariCPMonitor {

    private final HikariDataSource dataSource;

    @PostConstruct
    public void logConnectionPoolStats() {
        log.info("\n" +
                        "============================================================\n" +
                        "🔥 HikariCP Connection Pool Status\n" +
                        "------------------------------------------------------------\n" +
                        "🟢 최대 풀 크기 (maximumPoolSize)    : {}\n" +
                        "🟢 최소 유휴 커넥션 (minimumIdle)    : {}\n" +
                        "🔵 현재 사용 중 (activeConnections) : {}\n" +
                        "🟡 현재 유휴 상태 (idleConnections) : {}\n" +
                        "🔴 총 커넥션 (totalConnections)     : {}\n" +
                        "⚡ 대기 중인 요청 (waitingThreads)   : {}\n" +
                        "============================================================",
                dataSource.getMaximumPoolSize(),
                dataSource.getMinimumIdle(),
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }
}
