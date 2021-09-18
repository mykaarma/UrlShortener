package com.mykaarma.urlshortener.server.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mykaarma.urlshortener.model.jpa.UrlAttributes;


import java.util.*;


@Repository
public interface UrlRepository extends MongoRepository<UrlAttributes,Long> {
	
		

		public void deleteBySecondaryId(long id);
		
		
		public List<UrlAttributes> findByLongUrl(String longUrl);
	
		

		public List<UrlAttributes> findBySecondaryId(long id);
		
		
		
		

}
