package com.mykaarma.urlshortener.persistence;


import com.mykaarma.urlshortener.exception.ShortUrlException;
import com.mykaarma.urlshortener.model.AvailableHashPool;

public interface AvailableHashPoolAdapter {

	public AvailableHashPool fetchAvailableShortUrlHash() throws ShortUrlException;
	
	public void addHashToPool(String shortUrlHash) throws ShortUrlException;
	
	public void removeHashFromPool(String shortUrlHash) throws ShortUrlException;
	
	public int countAvailableHashes() throws ShortUrlException;
	
	public void deleteUsedHashes() throws ShortUrlException;
}
