package com.mykaarma.urlshortener.util;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mykaarma.urlshortener.enums.UrlErrorCodes;
import com.mykaarma.urlshortener.exception.ShortUrlException;
import com.mykaarma.urlshortener.exception.ShortUrlInternalServerException;
import com.mykaarma.urlshortener.persistence.ShortUrlCacheAdapter;
import com.mykaarma.urlshortener.persistence.ShortUrlDatabaseAdapter;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Component
@Setter
@Slf4j
public class UrlServiceUtil {
	
	private ShortUrlDatabaseAdapter urlRepository;
	
	@Autowired
	public UrlServiceUtil(ShortUrlDatabaseAdapter urlRepository) {
		
		this.urlRepository = urlRepository;
	}
	
	private String randomAlphabet;
	private String blackListedWordsFileUrl;
	private static final String DEFAULT_RANDOM_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-";
	private static String[] blackListedWords = null;
	SecureRandom sr = new SecureRandom();

	/**
	 * Checks whether the URL is valid or not
	 * 
	 * @param longUrl
	 * @return whether the URL is valid or not (boolean)
	 */
	public boolean isUrlValid(String longUrl) {
		try {
            new URL(longUrl).toURI();
            return true;
        }
        catch (Exception e) {
            return false;
        }
	}
	
	/**
	 * Checks whether the expiryDuration is valid or not
	 * 
	 * @param expiryDuration
	 * @return whether the expiry duration is valid or not
	 */
	public boolean isExpiryDurationValid(long expiryDuration) {

		return expiryDuration>0;
	}
	
	/**
	 * Finds the expiryDate from the expiryDuration
	 * 
	 * @param expiryDuration
	 * @return expiryDate
	 */
	public Date findExpiryDate(long expiryDuration) {
		
		Date date = new Date();
		long time = date.getTime();
        Date expiryDate = new Date();
        expiryDate.setTime(time + expiryDuration*1000);
        return expiryDate;
	}
	
	/**
	 * 
	 * @param date1
	 * @param date2
	 * @return difference between dates
	 */
	public long getDurationBetweenTwoDatesInSeconds(Date date1, Date date2) {
		
		return (date2.getTime() - date1.getTime())/1000;
	}
	
	/**
	 * Generates a random ID corresponding to the required hashLength
	 * 
	 * @param hashLength
	 * @return randomId
	 */
	public long getRandomId(int hashLength) throws ShortUrlException {
		
		long upperBound = (long)Math.pow(64, hashLength) - 1;
		long id = sr.nextLong(upperBound);
		return id;
	}
	
	/**
	 * Converts id to hash
	 * 
	 * @param id
	 * @param hashLength
	 * @return hash
	 */
	public String convertIdToHash(long id, int hashLength) {
		if(randomAlphabet==null) {
			randomAlphabet = DEFAULT_RANDOM_ALPHABET;
		}
		StringBuilder hash = new StringBuilder();
		while (id > 0) {
			hash.insert(0, randomAlphabet.charAt((int) (id % 64)));
			id = id / 64;
        }
        while(hash.length() < hashLength) {
        	hash.insert(0, randomAlphabet.charAt(0));
        }
        return hash.toString();
	}
	
	/**
	 * Converts hash to id
	 * 
	 * @param hash
	 * @return id
	 */
	public long convertHashToId(String hash)
	{
		long id=0;
		 for (int i = 0; i < hash.length(); i++) {
	            id = id * 64 + randomAlphabet.indexOf(hash.charAt(i));
	        }
		 return id;
		
	}
	
	/**
	 * Checks whether the hash is valid or not
	 * 
	 * @param hash
	 * @return whether the hash is valid or not (boolean)
	 * @throws ShortUrlInternalServerException
	 */
	public boolean isHashValid(String hash) throws ShortUrlException {
		
		if(urlRepository.existsByShortUrlHash(hash)) {return false;}
		
		if(hash.length()>20||hash.isEmpty()) {return false;}
		for(int i=0;i<hash.length();i++)
		{
			if(!((hash.charAt(i)>='a'&&hash.charAt(i)<='z')||(hash.charAt(i)>='A'&&hash.charAt(i)<='Z')||(hash.charAt(i)>='0'&&hash.charAt(i)<='9')||(hash.charAt(i)=='+')||(hash.charAt(i)=='-')))
			{
				return false;
			}
			
		}
		
		if(blackListedWords == null) {
			fetchBlackListedWords();
		}
		if(blackListedWords!=null) {
			for (String word: blackListedWords) {
				if (hash.toLowerCase().contains(word)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Fetches the blacklisted words
	 * 
	 * @throws ShortUrlInternalServerException
	 */
	public void fetchBlackListedWords() throws ShortUrlInternalServerException { 
		
		if(this.blackListedWordsFileUrl==null) {
			log.warn("blacklisted words file url not provided. Not checking for blacklisted words");
			return;
		}
			
		String blackListedWordsList = null;
		try {
		URL url = new URL(this.blackListedWordsFileUrl);
		
		
		blackListedWordsList = IOUtils.toString(url.openStream(), StandardCharsets.UTF_8);
		}
		catch(Exception e)
		{
			throw  new ShortUrlInternalServerException(UrlErrorCodes.SHORT_URL_INTERNAL_SERVER_ERROR);
		}
	
		
		if(blackListedWordsList!=null && !blackListedWordsList.isEmpty()){
			blackListedWords = blackListedWordsList.split(",");
			if(blackListedWords!=null && blackListedWords.length!=0){
				for(int index=0;index<blackListedWords.length;index++){
					String temp = blackListedWords[index];	
					blackListedWords[index] = temp.toLowerCase();
				}
			}
		}	
	}
	
}
