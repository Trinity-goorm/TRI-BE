package com.trinity.ctc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class TreadPoolConfig {
    private static final int CORE_POOL_SIZE = 1;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 100;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(CORE_POOL_SIZE);
        taskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
        taskExecutor.setQueueCapacity(QUEUE_CAPACITY);

        taskExecutor.initialize();
        return taskExecutor;
    }
}
