package com.mykaarma.urlshortener.persistence;


import com.mykaarma.urlshortener.exception.ShortUrlException;
import com.mykaarma.urlshortener.model.UrlDetails;

public interface ShortUrlDatabaseAdapter {
	
	public void saveUrl(UrlDetails urlDetails) throws ShortUrlException;
	
	public UrlDetails getLongUrlByShortUrlHash(String shortUrlHash) throws ShortUrlException;
	
	public UrlDetails getShortUrlByLongUrlAndBusinessUUID(String longUrl, String businessUUID) throws ShortUrlException;
	
	public boolean existsByShortUrlHash(String shortUrlHash) throws ShortUrlException;
	
}
