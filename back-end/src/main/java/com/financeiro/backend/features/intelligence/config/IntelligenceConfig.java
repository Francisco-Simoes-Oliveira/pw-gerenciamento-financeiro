package com.financeiro.backend.features.intelligence.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IntelligenceConfig {
    @Bean(name = "financialInsightExecutor", destroyMethod = "shutdown")
    ExecutorService financialInsightExecutor() {
        return Executors.newFixedThreadPool(4, Thread.ofPlatform().name("financial-insight-", 0).factory());
    }
}
