package com.hhplus.concert.config;

import com.hhplus.concert.core.domain.reservation.DistributedLock;
import com.hhplus.concert.core.infrastructure.redis.reservation.ReservationRockUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://localhost:6379");
        return Redisson.create(config);
    }

    @Bean
    public DistributedLock distributedLock(RedissonClient redissonClient) {
        return new ReservationRockUtil(redissonClient);
    }
}