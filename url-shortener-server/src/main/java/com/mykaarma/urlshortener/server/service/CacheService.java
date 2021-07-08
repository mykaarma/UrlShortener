package com.mykaarma.urlshortener.server.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CacheService {
	
	@CacheEvict(value="URL",key="#id")
	public void purgeUrlCache(long id)
	{
		log.info("Purging Cache for Id ={} ",id);
	}

}
