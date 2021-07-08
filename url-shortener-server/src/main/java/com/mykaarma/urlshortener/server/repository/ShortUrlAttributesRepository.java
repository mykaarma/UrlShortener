package com.mykaarma.urlshortener.server.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.mykaarma.urlshortener.model.jpa.ShortUrlAttributes;
@Repository
public interface ShortUrlAttributesRepository extends MongoRepository<ShortUrlAttributes, Long> {
	
	 public  List<ShortUrlAttributes> findById(long id);
}
