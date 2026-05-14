package com.codewithmd.blogger.bloggerappsapis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisHealthCheck implements ApplicationRunner {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // Log which Redis host Spring Boot is connecting to
            logger.info("=== REDIS CONFIG ===");
            logger.info("Redis Connection Factory: {}",
                    redisConnectionFactory.getClass().getSimpleName());

            // Try a simple ping
            String pong = redisConnectionFactory
                    .getConnection()
                    .ping();
            logger.info("Redis PING response: {}", pong);

            // Try writing a test key
            redisTemplate.opsForValue().set("health:check", "ok");
            Object value = redisTemplate.opsForValue().get("health:check");
            logger.info("Redis WRITE test: {}", value);

            // Clean up test key
            redisTemplate.delete("health:check");
            logger.info("=== REDIS CONNECTED SUCCESSFULLY ===");

        } catch (Exception e) {
            logger.error("=== REDIS CONNECTION FAILED ===", e);
            logger.error("Redis error: {}", e.getMessage());
        }
    }
}