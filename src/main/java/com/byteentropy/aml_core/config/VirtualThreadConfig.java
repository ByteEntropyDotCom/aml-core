package com.byteentropy.aml_core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;

@Configuration
@EnableAsync // Required to support @Async throughout the project
@ConditionalOnProperty(
    value = "spring.threads.virtual.enabled", 
    havingValue = "true"
)
public class VirtualThreadConfig {

    /**
     * Replaces the standard TaskExecutor with one that spawns 
     * a new Virtual Thread for every task.
     */
    @Bean
    public AsyncTaskExecutor applicationTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}