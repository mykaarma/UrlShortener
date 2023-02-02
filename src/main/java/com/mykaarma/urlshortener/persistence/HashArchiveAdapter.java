package com.mykaarma.urlshortener.persistence;


import com.mykaarma.urlshortener.exception.ShortUrlException;

public interface HashArchiveAdapter {

	public void addHashToArchive(String shortUrlHash) throws ShortUrlException;
	
	public boolean isHashUsed(String shortUrlHash) throws ShortUrlException;
}
