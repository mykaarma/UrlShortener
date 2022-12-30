package com.mykaarma.urlshortener.persistence;


import com.mykaarma.urlshortener.exception.ShortUrlException;
import com.mykaarma.urlshortener.model.UrlDetails;

public interface ShortUrlDatabaseAdapter {
	
	public void saveUrl(UrlDetails urlDetails) throws ShortUrlException;
	
	public UrlDetails getLongUrlBySecondaryId(long secondaryId) throws ShortUrlException;
	
	public UrlDetails getShortUrlByLongUrlAndBusinessId(String longUrl, String businessID) throws ShortUrlException;
	
	public boolean existsBySecondaryId(long secondaryId) throws ShortUrlException;
	
}
