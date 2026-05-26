package com.sparta.hubservice.global.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                )
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                // 경로 탐색 결과: 전체 허브 로드 + 그래프 탐색
                "hubRoutes", defaults.entryTtl(Duration.ofHours(1)),
                // 허브 단건 조회
                "hubs", defaults.entryTtl(Duration.ofHours(6))
        );

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaults.entryTtl(Duration.ofHours(1)))
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
