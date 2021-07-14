package com.mykaarma.urlshortener.server.util;

import java.net.URL;
import java.security.SecureRandom;
import java.util.*;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


import com.mykaarma.urlshortener.model.enums.UrlErrorCodes;
import com.mykaarma.urlshortener.model.exception.InternalServerException;
import com.mykaarma.urlshortener.model.jpa.UrlAttributes;
import com.mykaarma.urlshortener.model.redis.UrlDetails;
import com.mykaarma.urlshortener.server.repository.UrlDetailsRepository;
import com.mykaarma.urlshortener.server.repository.UrlRepository;

import io.micrometer.core.instrument.util.IOUtils;

@Component

public class UrlServiceUtil {
	@Autowired
	public UrlRepository repository;
	
	@Autowired
	public UrlDetailsRepository urlDetailsRepository;
	
	String ramdomAlphabet="Mheo9PI2qNs5Zpf80TBn7lmRbtQ4YKXHvwAEWxuzdra316OJigGLSVUCyFjkDc+-";
	private static final String BLACK_LISTED_WORDS_FILE_URL = "https://static.mykaarma.dev/blacklisted-words.txt";
	private static String[] blackListedWords = null;
	SecureRandom sr = new SecureRandom();

	/**
	 * @param longURL
	 * @return Whether the Url is Valid or Not (boolean)
	 */
	public  boolean isUrlValid(String longURL)
	{
		 try {
	            new URL(longURL).toURI();
	            return true;
	        }
	      
	        catch (Exception e) {
	            return false;
	        }
		 
	}
	
	/**
	 * @param urlDuration
	 * @return whether the Duration Format is Valid or Not
	 */
	public boolean isDurationValid(String urlDuration)
	{
		
		boolean isValid=true;
		if(urlDuration.length()>1000) {isValid= false;}
		int i=0;
		int count=0;
		long currentLengthOfDigits=0;
		boolean isLastCharDigit=false;
	
		while(i<urlDuration.length()&&urlDuration.length()<=1000)
		{
			if(urlDuration.charAt(i)==':')
			{
				if(!isLastCharDigit||(currentLengthOfDigits>8)) {isValid=false;}
				isLastCharDigit=false;
				
				currentLengthOfDigits=0;
				count++;
				i++;
					
			}
			else if(urlDuration.charAt(i)>='0'&&urlDuration.charAt(i)<='9')
			{
				currentLengthOfDigits++;
				isLastCharDigit=true;
				i++;
				
				
			}
			else {isValid=false;}
			
			
		}
		
		if(currentLengthOfDigits>8) {isValid=false;}
		
		if(count!=5||!isLastCharDigit) {isValid=false;}
		return isValid;
		
		
		
	}
	
	
	
	
	public long  findExpiryDurationInSeconds(String expiryDuration)
	{
		
		int i=0;
		int count=0;
		
		long durationInSeconds=0;
        
		while(i<expiryDuration.length())
		{
			
			
			int j=i;
			while(j<expiryDuration.length()&&expiryDuration.charAt(j)!=':')
			{
				
				j++;
			}
			
			
			String currentDuration=expiryDuration.substring(i,j);
			
			
			
			int duration=Integer.parseInt(currentDuration);
			
			
			
	        if(count==0)
	        {
	        	durationInSeconds+=duration*12*30*24*60*60;
	        	
	        }
	        else if(count==1)
	        {
	        	durationInSeconds+=duration*30*24*60*60;
	        }
	        else if(count==2)
	        {
	        	durationInSeconds+=duration*24*60*60;
	        }
	        else if(count==3)
	        {
	        	durationInSeconds+=duration*60*60;
	        }
	        else if(count==4)
	        {
	        	durationInSeconds+=duration*60;
	        }
	        else
	        {
	        	durationInSeconds+=duration;
	        	
	        }
	        i=j+1;
	      
	        
	        count++;
		
	}
		return durationInSeconds;
		
		
	
	}
	
	

	
	/**
	 * @return A randomly Generated Long Number
	 */
	public long getRandomId()
	{
		long min=20000000;
		long max=Long.MAX_VALUE;
		long id;
		 long randomLong = sr.nextLong();
		do {
			
			id=(long)((((double)randomLong/max)* (max - min)) + min);
			if(id<0) {id*=-1;}
			
			
		}
		while(repository.existsById(id));
		return id;
		
		
	}
	
	/**
	 * @param id
	 * @return THE Hash obtained from Base64 conversion of id
	 */
	public String convertToHash(long id)
	{
		//using base 64 conversion
		StringBuilder hash = new StringBuilder();
		 while (id > 0) {
	            hash.insert(0, ramdomAlphabet.charAt((int) (id % 64)));
	            id = id / 64;
	        }
	        return hash.toString();
		
		
	}
	
	
	/**
	 * @param hash
	 * @return the Base10 conversion of the hash
	 */
	public long convertHashToId(String hash)
	{
		long id=0;
		 for (int i = 0; i < hash.length(); i++) {
	            id = id * 64 + ramdomAlphabet.indexOf(hash.charAt(i));
	        }
		 return id;
	        
		
		
		
	}
	
	
	/**
	 * @param expiryDuration
	 * @return The Date when the Url will Expire
	 */
	public Date findExpiryDate(String expiryDuration)
	{
		
		int i=0;
		int count=0;
		Date date = new Date();
		Calendar c = Calendar.getInstance();
        c.setTime(date);
        
		while(i<expiryDuration.length())
		{
			
			
			int j=i;
			while(j<expiryDuration.length()&&expiryDuration.charAt(j)!=':')
			{
				
				j++;
			}
			
			
			String currentDuration=expiryDuration.substring(i,j);
			
			
			
			int duration=Integer.parseInt(currentDuration);
			
			
			
	        if(count==0)
	        {
	        	c.add(Calendar.YEAR, duration);
	        	
	        }
	        else if(count==1)
	        {
	        	c.add(Calendar.MONTH, duration);
	        }
	        else if(count==2)
	        {
	        	c.add(Calendar.DATE, duration);
	        }
	        else if(count==3)
	        {
	        	c.add(Calendar.HOUR, duration);
	        }
	        else if(count==4)
	        {
	        	c.add(Calendar.MINUTE, duration);
	        }
	        else
	        {
	        	c.add(Calendar.SECOND, duration);
	        	
	        }
	        i=j+1;
	      
	        
	        count++;
		
	}
		return c.getTime();
		
		
	
	}
	
	/**
	 * @param shortUrl
	 * @return Extracted hash from the shortUrl
	 */
	public String extractHash(String shortUrl)
	{
		int hashStartIndex=-1;
		for(int i=0;i<shortUrl.length();i++)
		{
			if(shortUrl.charAt(i)=='/') {hashStartIndex=i+1;}
			
		}
		if(hashStartIndex==-1||hashStartIndex>=shortUrl.length()) {return "NOT_FOUND";}
		return shortUrl.substring(hashStartIndex);
		
		
		
		
		
	}
	
	/**
	 * @param hash
	 * @return whether the hash provided is a valid Base64 hash or not and if it contains any blacklisted word or not
	 * @throws Exception
	 */
	public boolean isValid(String hash) throws InternalServerException
	{
		
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
		for (String word: blackListedWords) {
			if (hash.toLowerCase().contains(word)) {
				return false;
			}
		}
		
		
		
		return true;
	}
	
	
	
	
	/**
	 * 
	 * @throws Exception
	 * 
	 */
	public static  void fetchBlackListedWords() throws InternalServerException { 
		
		String blackListedWordsList = null;
		try {
		URL url = new URL(BLACK_LISTED_WORDS_FILE_URL);
		
		
		blackListedWordsList = IOUtils.toString(url.openStream());
		}
		catch(Exception e)
		{
			throw  new InternalServerException(UrlErrorCodes.INTERNAL_SERVER_ERROR);
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
	
	@Async
	public void incrementClickCount(UrlDetails urlDetails)
	{
		long id =urlDetails.getSecondaryId();
		
	List<UrlAttributes>sameUrlAttributes =repository.findBySecondaryId(id);
	
	if(sameUrlAttributes!=null&&!sameUrlAttributes.isEmpty())
	{
		
		sameUrlAttributes.get(0).incrementClickCount();
		repository.save(sameUrlAttributes.get(0));
	}
	
	urlDetails.incrementClickCount();
	
	urlDetailsRepository.save(urlDetails);

		
	}
	
	
	
	
	
	

}
