package com.trinity.ctc.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
@EnableAsync
public class TreadPoolConfig {
    @Bean(name = "reservation-event-listener")
    public Executor reservationEventListener() {
        return Executors.newWorkStealingPool();
    }


    @Bean(name = "reservation-completed-notification")
    public Executor reservationCompletedNotificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setThreadNamePrefix("reservation-complete-notification-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadPriority(7);

        executor.initialize();
        return executor;
    }

    @Bean(name = "reservation-canceled-notification")
    public Executor reservationCanceledNotificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setThreadNamePrefix("reservation-canceled-notification-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadPriority(7);

        executor.initialize();
        return executor;
    }

    @Bean(name = "daily-reservation-notification")
    public Executor dailyReservationNotificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(20);
        executor.setThreadNamePrefix("daily-reservation-notification-");
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadPriority(6);

        executor.initialize();
        return executor;
    }

    @Bean(name = "hourly-reservation-notification")
    public Executor hourlyTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(10);
        executor.setThreadNamePrefix("daily-reservation-notification-");
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(60);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadPriority(6);

        executor.initialize();
        return executor;
    }

    @Bean(name = "empty-seat-notification")
    public Executor seatTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(20);
        executor.setThreadNamePrefix("empty-seat-notification-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadPriority(10);

        executor.initialize();
        return executor;
    }

    @Bean(name = "response-handler")
    public Executor singleResponseTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(20);
        executor.setThreadNamePrefix("response-handler-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadPriority(8);

        executor.initialize();
        return executor;
    }

    @Bean(name = "immediate-retry")
    public Executor retryImmediatelyTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(8);
        executor.setThreadNamePrefix("immediate-retry-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadPriority(9);

        executor.initialize();
        return executor;
    }

    @Bean(name = "delayed-retry")
    public Executor retryDelayTaskExecutor() {
        return Executors.newCachedThreadPool();
    }

    @Bean(name = "save-notification-history")
    public Executor saveNotificationHistoryTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(2);
        executor.setThreadNamePrefix("save-notification-history-");
        executor.setWaitForTasksToCompleteOnShutdown(true);

        executor.initialize();
        return executor;
    }
}
