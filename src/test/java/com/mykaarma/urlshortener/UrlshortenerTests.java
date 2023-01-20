package com.mykaarma.urlshortener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.mykaarma.urlshortener.exception.ShortUrlException;
import com.mykaarma.urlshortener.model.ShortUrl;
import com.mykaarma.urlshortener.model.UrlDetails;
import com.mykaarma.urlshortener.persistence.ShortUrlCacheAdapter;
import com.mykaarma.urlshortener.persistence.ShortUrlDatabaseAdapter;
import com.mykaarma.urlshortener.service.UrlService;
import com.mykaarma.urlshortener.util.UrlServiceUtil;

@SpringBootTest
class UrlshortenerTests {
	
	@Autowired
	UrlServiceUtil urlServiceUtil;
	
	@Autowired
	UrlService urlService;
	
	@MockBean
	ShortUrlDatabaseAdapter mockRepository;
	
	@MockBean
	ShortUrlCacheAdapter shortUrlCacheAdapter;
	
	@Test
	void generateShortUrlHash() throws ShortUrlException {
		System.out.println("\n\nGenerateShortUrlHash");
		long randomId = urlServiceUtil.getRandomId(7);
		String shortUrlHash = urlServiceUtil.convertIdToHash(randomId, 7);
		System.out.println(shortUrlHash);
	}
	
	@Test
	void shortenUrlTestNew() throws ShortUrlException {
		System.out.println("\n\nshortenUrlTestNew");
		ShortUrl shortUrl = urlService.shortenUrl("https://google.com/sieifnqinfiniqewfniqeinf", "myk.com", 3600, "abcdef", null, false, 6, "https://static.mykaarma.dev/blacklisted-words.txt", null, "v2");
		System.out.println("Short URL generated successfully");
		System.out.println("Short URL: "+shortUrl.getShortUrl());
		System.out.println("Expiry Date: "+shortUrl.getExpiryDate());
	}
	
	@Test
	void shortenUrlTestWithOverwrite() throws ShortUrlException {
		System.out.println("\n\nshortenUrlTestExistingWithOverwrite");
		UrlDetails urlDetails = new UrlDetails("a1b2c3", "https://abcd.com/ABUAFDfdv", "myk.com/a1b2c3", new Date(), urlServiceUtil.findExpiryDate(7200), "abcdef", null, null, true);
		List<UrlDetails> urlDetailsList = new ArrayList<>();
		urlDetailsList.add(urlDetails);
		Mockito.when(mockRepository.getUrlDetailsByLongUrlAndBusinessUUID("https://abcd.com/ABUAFDfdv", "abcdef")).thenReturn(urlDetailsList);
		ShortUrl shortUrl = urlService.shortenUrl("https://abcd.com/ABUAFDfdv", "myk.com", 3600, "abcdef", null, true, 6, "https://static.mykaarma.dev/blacklisted-words.txt", null, "v2");
		System.out.println("Short URL generated successfully");
		System.out.println("Short URL: "+shortUrl.getShortUrl());
		System.out.println("Expiry Date: "+shortUrl.getExpiryDate());
	}
	
	@Test
	void shortenUrlTestWithoutOverwriteWithTracking() throws ShortUrlException {
		System.out.println("\n\nshortenUrlTestExistingWithoutOverwrite");
		UrlDetails urlDetails = new UrlDetails("a1b2c3", "https://abcd.com/ABUAFDfdv", "myk.com/a1b2c3", new Date(), urlServiceUtil.findExpiryDate(7200), "abcdef", null, null, true);
		List<UrlDetails> urlDetailsList = new ArrayList<>();
		urlDetailsList.add(urlDetails);
		Mockito.when(mockRepository.getUrlDetailsByLongUrlAndBusinessUUID("https://abcd.com/ABUAFDfdv", "abcdef")).thenReturn(urlDetailsList);
		ShortUrl shortUrl = urlService.shortenUrl("https://abcd.com/ABUAFDfdv", "myk.com", 3600, "abcdef", null, false, 6, "https://static.mykaarma.dev/blacklisted-words.txt", null, "v2");
		System.out.println("Short URL generated successfully");
		System.out.println("Short URL: "+shortUrl.getShortUrl());
		System.out.println("Expiry Date: "+shortUrl.getExpiryDate());
	}
	
	@Test
	void getRedirectingHtmlForLongUrl() throws ShortUrlException {
		String shortUrlHash = "a1b2c3";
		System.out.println("\n\ngetRedirectingHtmlForLongUrl");
		UrlDetails urlDetails = new UrlDetails("a1b2c3", "https://abcd.com/ABUAFDfdv", "myk.com/a1b2c3", new Date(), urlServiceUtil.findExpiryDate(7200), "abcdef", null, null, true);
		List<UrlDetails> urlDetailsList = new ArrayList<>();
		urlDetailsList.add(urlDetails);
		Mockito.when(mockRepository.getUrlDetailsByShortUrlHash(shortUrlHash)).thenReturn(urlDetailsList);
		String htmlResponse = urlService.getHtmlForRedirectingToLongUrl(shortUrlHash);
		System.out.println("HTML Response: "+htmlResponse);
	}
	
	@Test
	void cacheTest() throws ShortUrlException {
		String shortUrlHash = "a1b2c3";
		System.out.println("\n\nCache Test");
		UrlDetails urlDetails = new UrlDetails("a1b2c3", "https://abcd.com/ABUAFDfdv", "myk.com/a1b2c3", new Date(), urlServiceUtil.findExpiryDate(7200), "abcdef", null, null, true);
		List<UrlDetails> urlDetailsList = new ArrayList<>();
		urlDetailsList.add(urlDetails);
		Mockito.when(mockRepository.getUrlDetailsByShortUrlHash(shortUrlHash)).thenReturn(urlDetailsList);
		String htmlResponse = urlService.getHtmlForRedirectingToLongUrl(shortUrlHash);
		System.out.println("HTML Response: "+htmlResponse);
		
		htmlResponse = urlService.getHtmlForRedirectingToLongUrl(shortUrlHash);
		System.out.println("HTML Response: "+htmlResponse);
	}

}
