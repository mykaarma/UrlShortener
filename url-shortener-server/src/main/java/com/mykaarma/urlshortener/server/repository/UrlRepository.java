package com.mykaarma.urlshortener.server.repository;


import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mykaarma.urlshortener.model.jpa.UrlAttributes;


import java.util.*;


@Repository
public interface UrlRepository extends MongoRepository<UrlAttributes,Long> {
	
		
		@CacheEvict(value="URL",key="#id")
		public void deleteBySecondaryId(long id);
		
		
		public List<UrlAttributes> findByLongUrl(String longUrl);
	
		
		@Cacheable("URL")
		public List<UrlAttributes> findBySecondaryId(long id);
		
		
		
		

}
