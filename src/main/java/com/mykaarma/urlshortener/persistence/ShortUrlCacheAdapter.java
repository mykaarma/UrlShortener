package com.mykaarma.urlshortener.persistence;

import com.mykaarma.urlshortener.exception.ShortUrlException;

public interface ShortUrlCacheAdapter {

	public void saveInCache(String shortUrlHash, String longUrl) throws ShortUrlException;
	
	public String getLongUrl(String shortUrlHash) throws ShortUrlException;
}
