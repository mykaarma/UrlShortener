package com.mykaarma.urlshortener.service;

import java.util.concurrent.locks.Lock;

import com.mykaarma.urlshortener.enums.RegistryKey;

public interface RedisLockService {

	public Lock tryLockOnEntity(String redisKey, RegistryKey registryKey);
	
	public boolean unlock(Lock lock);
}
