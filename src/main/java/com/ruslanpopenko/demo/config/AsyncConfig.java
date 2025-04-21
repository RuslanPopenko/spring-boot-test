package com.ruslanpopenko.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "limitedTaskExecutor")
    @Scope("prototype")
    public AsyncTaskExecutor limitedTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);      // Always maintain 10 threads
        executor.setMaxPoolSize(10);       // Never grow beyond 10 threads
        executor.setQueueCapacity(100);    // Queue up to 100 additional tasks
        executor.setThreadNamePrefix("limited-executor-");
        executor.initialize();
        return executor;
    }

}
