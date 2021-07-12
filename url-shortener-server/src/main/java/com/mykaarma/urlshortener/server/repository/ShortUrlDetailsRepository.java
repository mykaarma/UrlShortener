package com.mykaarma.urlshortener.server.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.mykaarma.urlshortener.model.redis.ShortUrlDetails;
import com.mykaarma.urlshortener.model.redis.UrlDetails;


public interface ShortUrlDetailsRepository extends CrudRepository<ShortUrlDetails, Long>{

	
	
}
