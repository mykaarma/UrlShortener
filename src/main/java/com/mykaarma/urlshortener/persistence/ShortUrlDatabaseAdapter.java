package com.mykaarma.urlshortener.persistence;


import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.mykaarma.urlshortener.exception.ShortUrlException;
import com.mykaarma.urlshortener.model.UrlDetails;

public interface ShortUrlDatabaseAdapter {
	
	public void saveUrl(UrlDetails urlDetails) throws ShortUrlException;
	
	public List<UrlDetails> getUrlDetailsByShortUrlHash(String shortUrlHash) throws ShortUrlException;
	
	public List<UrlDetails> getActiveUrlDetailsByLongUrlAndBusinessUUID(String longUrl, String businessUUID) throws ShortUrlException;
	
	public boolean existsByShortUrlHash(String shortUrlHash) throws ShortUrlException;
	
	public void updateLastAccessedTimestamp(UrlDetails urlDetails, Date lastAccessedTimestamp) throws ShortUrlException;
	
}
