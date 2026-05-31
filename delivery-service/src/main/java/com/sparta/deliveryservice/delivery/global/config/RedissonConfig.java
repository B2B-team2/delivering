package com.sparta.deliveryservice.delivery.global.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    private final String redisHost;
    private final int redisPort;
    private final String redisPassword;

    public RedissonConfig(
            @Value("${REDIS_HOST}") String redisHost,
            @Value("${REDIS_PORT}") int redisPort,
            @Value("${REDIS_PASSWORD}") String redisPassword) {
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.redisPassword = redisPassword;
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setPassword(redisPassword);
        return Redisson.create(config);
    }
}