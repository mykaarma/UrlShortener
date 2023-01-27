package com.mykaarma.urlshortener.persistence;

import org.springframework.stereotype.Repository;

import com.mykaarma.urlshortener.exception.ShortUrlException;
import com.mykaarma.urlshortener.model.UrlDetails;

public interface ShortUrlCacheAdapter {

	public void saveInCache(String shortUrlHash, UrlDetails urlDetails, long ttl) throws ShortUrlException;
	
	public UrlDetails fetchUrlDetailsFromCache(String shortUrlHash) throws ShortUrlException;
}
