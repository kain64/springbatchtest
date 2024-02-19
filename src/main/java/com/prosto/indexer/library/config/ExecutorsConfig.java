package com.prosto.indexer.library.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ExecutorsConfig {
    private static final Logger log = LoggerFactory.getLogger(ExecutorsConfig.class);
    @Bean("manualTaskExecutor")
    public TaskExecutor manualTaskExecutor() {
        log.info("taskExecutor init");
        var taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(100);
        return taskExecutor;
    }
}
