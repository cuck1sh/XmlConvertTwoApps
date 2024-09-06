package com.example.secondapp.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMqConfig {
    @Bean
    public Queue makeQueue() {
        return new Queue("conversionQueue", false);
    }
}
