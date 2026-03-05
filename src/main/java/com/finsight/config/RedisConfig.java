package com.finsight.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Redis caching configuration. Enabled only when spring.data.redis.host is set.
 * Analytics queries can benefit from caching to reduce database load.
 *
 * To enable: set spring.cache.type=redis in application.properties
 * and ensure a Redis instance is running.
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisConfig {
    // Spring Boot auto-configures RedisCacheManager when spring-boot-starter-data-redis
    // is on the classpath and spring.cache.type=redis is set. No additional bean
    // definitions are needed for basic caching.
}
