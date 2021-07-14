package com.mykaarma.urlshortener.server.repository;

import org.springframework.data.repository.CrudRepository;

import com.mykaarma.urlshortener.model.redis.ShortUrlDetails;



public interface ShortUrlDetailsRepository extends CrudRepository<ShortUrlDetails, Long>{

	
	
}
