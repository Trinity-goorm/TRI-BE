package com.trinity.ctc.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

@Configuration
@EnableAsync
public class TreadPoolConfig {
    @Bean(name = "fixedThreadPoolExecutor")
    public Executor asyncExecutor1() {
        return Executors.newFixedThreadPool(1000);
    }

    @Bean(name = "fixedThreadPoolExecutor2")
    public Executor asyncExecutor2() {
        return Executors.newFixedThreadPool(16);
    }


    @Bean(name = "cachedThreadPoolExecutor")
    public Executor cachedThreadPoolExecutor() {
        return Executors.newCachedThreadPool();
    }

    @Bean(name = "workStealingThreadPoolExecutor")
    public Executor workStealingThreadPoolExecutor() {
        return Executors.newWorkStealingPool();
    }

    @Bean(name = "customThreadPoolExecutor")
    public Executor customThreadPoolExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("custom-Thread-");
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
