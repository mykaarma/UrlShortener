package com.mykaarma.urlshortener.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import com.mykaarma.urlshortener.model.dto.request.ShortenUrlRequestDTO;
import com.mykaarma.urlshortener.model.dto.response.ShortenUrlResponseDTO;
import com.mykaarma.urlshortener.server.service.UrlService;


import lombok.extern.slf4j.Slf4j;

//@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest(classes =UrlShortenerServer.class)
 class UrlServiceTests {
	
	@Autowired
	private UrlService urlService;
	
	 @Test
	    void contextLoads() throws Exception {
		 testShortUrl();
		 testFindLongUrl();
		 testFindClickCount();
	 }
	 
	 public void testShortUrl() throws Exception
	 {
		 ShortenUrlRequestDTO shortenUrlRequestDTO= new ShortenUrlRequestDTO();
		 shortenUrlRequestDTO.setBusinessId("xxnsi11kdek233d");
		 shortenUrlRequestDTO.setLongUrl("https://www.testApp.com");
		 shortenUrlRequestDTO.setShortUrlDomain("https://www.app.mykaarma.com");
		 shortenUrlRequestDTO.setExpiryDuration("1:3:2:11:0:3");
		 shortenUrlRequestDTO.setTrackingEnabled(false);
		 
		 ShortenUrlResponseDTO shortenUrlResponseDTO =  urlService.shortenUrl(shortenUrlRequestDTO);
		
		log.info(shortenUrlResponseDTO.getShortUrl());
		
		 
		 
	 }
	 
	 public void testFindLongUrl() throws Exception
	 {
		String shortUrlHash="yyaaxd1";
		 
		try {
		 String longUrl =  urlService.findLongUrl(shortUrlHash);
		 log.info("succesfully found longUrl={) for shortUrlHash={}",longUrl,shortUrlHash);
		 
		}
		catch(Exception e)
		{
			log.info("testFindLongUrl could not succesfully find Long Url for hash={}",shortUrlHash);
		}
		
		
		 
	 }
	 public void testFindClickCount() throws Exception
	 {
		String shortUrlHash="xxjwmq3";
		 
		try {
		 long clickCount =  urlService.findClickCounts(shortUrlHash);
		 log.info("click Count={} for shortUrlHash={}",clickCount,shortUrlHash);
		 
		}
		catch(Exception e)
		{
			log.info("testFindClickCount could not succesfully find Click Count for hash={}",shortUrlHash);
		}
		
		
		 
	 }
	 
}
