package com.trinity.ctc.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

@Configuration
@EnableAsync
public class TreadPoolConfig {
    @Bean(name = "sendingProcessThreadPool")
    public Executor asyncExecutor1() {
        return Executors.newFixedThreadPool(1000);
    }

    @Bean(name = "reservationSendingThreadPool")
    public Executor asyncExecutor2() {
        return Executors.newFixedThreadPool(1000);
    }

    @Bean(name = "resultProcessingThreadPool")
    public Executor asyncExecutor3() {
        return Executors.newFixedThreadPool(10);
    }


    @Bean(name = "customThreadPoolExecutor")
    public Executor customThreadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("custom-sending-Thread-");
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setThreadPriority(Thread.NORM_PRIORITY);
        executor.initialize();
        return executor;
    }

    @Bean(name = "fixCachedThreadPoolExecutor")
    public Executor fixCachedThreadPoolExecutor() {

        return new ThreadPoolExecutor(
                0,
                8,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
