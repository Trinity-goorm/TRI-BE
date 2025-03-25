package com.trinity.ctc.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class TreadPoolConfig {
    @Bean(name = "reservation-completed-notification")
    public Executor reservationCompletedTaskExecutor() {
        return Executors.newFixedThreadPool(1000);
    }

    @Bean(name = "reservation-canceled-notification")
    public Executor reservationCanceledTaskExecutor() {
        return Executors.newFixedThreadPool(1000);
    }

    @Bean(name = "daily-notification")
    public Executor dailyTaskExecutor() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean(name = "hourly-notification")
    public Executor hourlyTaskExecutor() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean(name = "seat-notification")
    public Executor seatTaskExecutor() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean(name = "firebase-sending")
    public Executor sendingTaskExecutor() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean(name = "single-response")
    public Executor singleResponseTaskExecutor() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean(name = "each-response")
    public Executor eachResponseTaskExecutor() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean(name = "multicast-response")
    public Executor multicastResponseTaskExecutor() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean(name = "retry-immediately")
    public Executor retryImmediatelyTaskExecutor() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean(name = "retry-delay")
    public Executor retryDelayTaskExecutor() {
        return Executors.newFixedThreadPool(10);
    }

    @Bean(name = "save-notification-history")
    public Executor saveNotificationHistoryTaskExecutor() {
        return Executors.newFixedThreadPool(10);
    }


//    @Bean(name = "each-response")
//    public Executor customThreadPoolExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//
//        executor.setCorePoolSize(50);
//        executor.setMaxPoolSize(100);
//        executor.setQueueCapacity(100);
//        executor.setThreadNamePrefix("custom-sending-Thread-");
//        executor.setAllowCoreThreadTimeOut(true);
//        executor.setWaitForTasksToCompleteOnShutdown(true);
//        executor.setAwaitTerminationSeconds(60);
//        executor.setThreadPriority(Thread.NORM_PRIORITY);
//        executor.initialize();
//        return executor;
//    }
//
//    @Bean(name = "fixCachedThreadPoolExecutor")
//    public Executor fixCachedThreadPoolExecutor() {
//
//        return new ThreadPoolExecutor(
//                0,
//                8,
//                60L,
//                TimeUnit.SECONDS,
//                new SynchronousQueue<>(),
//                new ThreadPoolExecutor.AbortPolicy()
//        );
//    }
}
