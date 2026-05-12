package com.mykaarma.urlshortener.persistence;


import java.util.Date;
import java.util.List;

import com.mykaarma.urlshortener.exception.ShortUrlDuplicateException;
import com.mykaarma.urlshortener.exception.ShortUrlException;
import com.mykaarma.urlshortener.model.UrlDetails;

public interface ShortUrlDatabaseAdapter {
	
	public void saveUrl(UrlDetails urlDetails) throws ShortUrlException;
	
	public UrlDetails getUrlDetailsByShortUrlHash(String shortUrlHash) throws ShortUrlException;

	/**
	 * Returns the persisted {@link UrlDetails} for {@code shortUrlHash} regardless of expiry
	 * or validity flags. Returns {@code null} when no row exists for the hash.
	 */
	public UrlDetails getUrlDetailsByShortUrlHashIncludingExpired(String shortUrlHash) throws ShortUrlException;

	public UrlDetails getActiveUrlDetailsByLongUrlAndBusinessUUIDAndDomain(String longUrl, String businessUUID, String shortUrlDomain) throws ShortUrlException;

	public UrlDetails getUrlDetailsByLongUrlAndBusinessUUIDAndDomain(String longUrl, String businessUUID, String shortUrlDomain) throws ShortUrlException;
	
	public void updateLastAccessedTimestamp(UrlDetails urlDetails, Date lastAccessedTimestamp) throws ShortUrlException;
	
}
