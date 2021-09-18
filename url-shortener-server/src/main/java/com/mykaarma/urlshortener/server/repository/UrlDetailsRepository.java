package com.mykaarma.urlshortener.server.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;


import com.mykaarma.urlshortener.model.redis.UrlDetails;

public interface UrlDetailsRepository extends CrudRepository<UrlDetails, Long>{

	public List<UrlDetails> findByLongUrl(String longUrl);
	public List<UrlDetails> findBySecondaryId(long id);
}
