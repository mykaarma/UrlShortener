package com.mykaarma.urlshortener;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

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
	void generateShortUrlHash() {
		System.out.println("\n\nGenerateShortUrlHash");
		long randomId = urlServiceUtil.getRandomId(7);
		String shortUrlHash = urlServiceUtil.convertIdToHash(randomId, 7);
		System.out.println(shortUrlHash);
	}
	
	@Test
	void shortenUrlTestNew() {
		System.out.println("\n\nshortenUrlTestNew");
		ShortUrl shortUrl = urlService.shortenUrl("https://google.com/sieifnqinfiniqewfniqeinf", "myk.com", 3600, "abcd", null, null, null, null, null, false, 6, "https://static.mykaarma.dev/blacklisted-words.txt", null, "v2");
		System.out.println("Short URL generated successfully");
		System.out.println("Short URL: "+shortUrl.getShortUrl());
		System.out.println("Expiry Date: "+shortUrl.getExpiryDate());
	}
	
	@Test
	void shortenUrlTestWithOverwrite() {
		System.out.println("\n\nshortenUrlTestExistingWithOverwrite");
		UrlDetails urlDetails = new UrlDetails(25, "https://abcd.com/ABUAFDfdv", "myk.com/a1b2c3", new Date(), urlServiceUtil.findExpiryDate(7200), "abcdef", null, null, null, null, null);
		Mockito.when(mockRepository.getShortUrlByLongUrlAndBusinessId("https://abcd.com/ABUAFDfdv", "abcdef")).thenReturn(urlDetails);
		ShortUrl shortUrl = urlService.shortenUrl("https://abcd.com/ABUAFDfdv", "myk.com", 3600, "abcdef", "click", null, "clickCat", null, null, true, 6, "https://static.mykaarma.dev/blacklisted-words.txt", null, "v2");
		System.out.println("Short URL generated successfully");
		System.out.println("Short URL: "+shortUrl.getShortUrl());
		System.out.println("Expiry Date: "+shortUrl.getExpiryDate());
	}
	
	@Test
	void shortenUrlTestWithoutOverwriteWithTracking() {
		System.out.println("\n\nshortenUrlTestExistingWithoutOverwrite");
		UrlDetails urlDetails = new UrlDetails(25, "https://abcd.com/ABUAFDfdv", "myk.com/a1b2c3", new Date(), urlServiceUtil.findExpiryDate(7200), "abcdef", null, null, null, null, null);
		Mockito.when(mockRepository.getShortUrlByLongUrlAndBusinessId("https://abcd.com/ABUAFDfdv", "abcdef")).thenReturn(urlDetails);
		ShortUrl shortUrl = urlService.shortenUrl("https://abcd.com/ABUAFDfdv", "myk.com", 3600, "abcdef", "click", null, "clickCat", null, null, false, 6, "https://static.mykaarma.dev/blacklisted-words.txt", null, "v2");
		System.out.println("Short URL generated successfully");
		System.out.println("Short URL: "+shortUrl.getShortUrl());
		System.out.println("Expiry Date: "+shortUrl.getExpiryDate());
	}
	
	@Test
	void getRedirectingHtmlForLongUrl() {
		String shortUrlHash = "a1b2c3";
		System.out.println("\n\ngetRedirectingHtmlForLongUrl");
		UrlDetails urlDetails = new UrlDetails(urlServiceUtil.convertHashToId(shortUrlHash), "https://abcd.com/ABUAFDfdv", "myk.com/a1b2c3", new Date(), urlServiceUtil.findExpiryDate(7200), "abcdef", null, null, null, null, null);
		Mockito.when(mockRepository.getLongUrlBySecondaryId(urlServiceUtil.convertHashToId(shortUrlHash))).thenReturn(urlDetails);
		String htmlResponse = urlService.getHtmlForRedirectingToLongUrl(shortUrlHash, null);
		System.out.println("HTML Response: "+htmlResponse);
	}
	
	@Test
	void cacheTest() {
		String shortUrlHash = "a1b2c3";
		System.out.println("\n\nCache Test");
		UrlDetails urlDetails = new UrlDetails(urlServiceUtil.convertHashToId(shortUrlHash), "https://abcd.com/ABUAFDfdv", "myk.com/a1b2c3", new Date(), urlServiceUtil.findExpiryDate(7200), "abcdef", null, null, null, null, null);
		Mockito.when(mockRepository.getLongUrlBySecondaryId(urlServiceUtil.convertHashToId(shortUrlHash))).thenReturn(urlDetails);
		String htmlResponse = urlService.getHtmlForRedirectingToLongUrl(shortUrlHash, null);
		System.out.println("HTML Response: "+htmlResponse);
		
		htmlResponse = urlService.getHtmlForRedirectingToLongUrl(shortUrlHash, null);
		System.out.println("HTML Response: "+htmlResponse);
	}

}
