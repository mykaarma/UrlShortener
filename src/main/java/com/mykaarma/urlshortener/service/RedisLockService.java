package com.mykaarma.urlshortener.service;

import com.mykaarma.urlshortener.model.RegistryKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockService {
    private final RedisConnectionFactory redisConnectionFactory;

    public Lock tryLockOnEntity(String redisKey, RegistryKey registryKey, Long lockExpiryInMillis) {
        try {
            log.info(" Trying to acquire lock registryKey={} redisKey={} ", registryKey.name(), redisKey);
            RedisLockRegistry registry = new RedisLockRegistry(redisConnectionFactory, registryKey.name(), lockExpiryInMillis);
            return registry.obtain(redisKey);
        } catch (Exception e) {
            log.warn(" Exception while obtaining lock ", e);
            return null;
        }
    }

    public boolean unlock(Lock lock) {
        try {
            log.info(" Trying to unlock lock={}", lock);
            if(lock != null) {
                lock.unlock();
            }
            return true;
        } catch (Exception e) {
            log.warn(" Unable to release lock ", e);
            return false;
        }
    }
}
