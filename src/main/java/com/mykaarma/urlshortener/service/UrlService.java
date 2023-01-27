package com.mykaarma.urlshortener.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mykaarma.urlshortener.enums.UrlErrorCodes;
import com.mykaarma.urlshortener.exception.BadRedirectingRequestException;
import com.mykaarma.urlshortener.exception.BadShorteningRequestException;
import com.mykaarma.urlshortener.exception.ShortUrlException;
import com.mykaarma.urlshortener.exception.ShortUrlNotFoundException;
import com.mykaarma.urlshortener.model.ShortUrl;
import com.mykaarma.urlshortener.model.UrlDetails;
import com.mykaarma.urlshortener.persistence.ShortUrlCacheAdapter;
import com.mykaarma.urlshortener.persistence.ShortUrlDatabaseAdapter;
import com.mykaarma.urlshortener.util.UrlServiceUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UrlService {
	
	private UrlServiceUtil urlServiceUtil;
	
	private ShortUrlDatabaseAdapter urlRepository;
	
	private ShortUrlCacheAdapter shortUrlCacheAdapter;
	
	@Autowired
	public UrlService(UrlServiceUtil urlServiceUtil, ShortUrlDatabaseAdapter urlRepository, ShortUrlCacheAdapter shortUrlCacheAdapter) {
		
		this.urlServiceUtil = urlServiceUtil;
		this.urlRepository = urlRepository;
		this.shortUrlCacheAdapter = shortUrlCacheAdapter;
	}
	
	/**
	 * Creating a shortUrl for the provided longUrl and businessUUID
	 * 
	 * @param longUrl
	 * @param shortUrlDomain
	 * @param expiryDuration
	 * @param businessUUID
	 * @param additionalParams
	 * @param overwrite
	 * @param hashLength
	 * @param blackListedWordsFileUrl
	 * @param randomAlphabet
	 * @param urlPrefix
	 * @return ShortUrl
	 * @throws ShortUrlException
	 */
	public ShortUrl shortenUrl(String longUrl, String shortUrlDomain, long expiryDuration, String businessUUID, Map<String, String> additionalParams,
			boolean overwrite, int hashLength, String blackListedWordsFileUrl, String randomAlphabet, String urlPrefix) throws ShortUrlException {
		
		this.urlServiceUtil.setBlackListedWordsFileUrl(blackListedWordsFileUrl);
		this.urlServiceUtil.setRandomAlphabet(randomAlphabet);
		
		if(longUrl==null || shortUrlDomain==null) {
			throw new BadShorteningRequestException(UrlErrorCodes.SHORT_URL_BAD_REQUEST);
		}
		
		if (!urlServiceUtil.isUrlValid(longUrl)) {
			log.error("Url={} is Malformed", longUrl);
			throw new BadShorteningRequestException(UrlErrorCodes.SHORT_URL_BAD_REQUEST);

		}
		
		if (!urlServiceUtil.isExpiryDurationValid(expiryDuration)) {
			log.error("Duration={} is Invalid for businessUUID={} , longUrl={}", expiryDuration, businessUUID, longUrl);
			throw new BadShorteningRequestException(UrlErrorCodes.SHORT_URL_BAD_REQUEST);

		}
		
		Date expiryDate = urlServiceUtil.findExpiryDate(expiryDuration);
		
		List<UrlDetails> existingShortUrls = urlRepository.getActiveUrlDetailsByLongUrlAndBusinessUUID(longUrl, businessUUID);
		
		if (!(existingShortUrls.isEmpty())) {
			
			UrlDetails existingShortUrl = existingShortUrls.get(0);
			
			log.info("ShortUrl={} already present for longUrl={} and businessUUID={}", existingShortUrl.getShortUrl(), existingShortUrl.getLongUrl(), existingShortUrl.getBusinessUUID());
			
			if(overwrite) {
				if (expiryDate.before(existingShortUrl.getExpiryDateTime())) {
					log.error("The provided expiryDate for the shortUrl = {} is before the existing expiryDate", existingShortUrl.getShortUrl());
				}
				
				
				log.warn("overwriting existing shortUrl details of shortUrl = {}", existingShortUrl.getShortUrl());
				existingShortUrl.setExpiryDateTime(expiryDate);
				existingShortUrl.setAdditionalParams(additionalParams);
			}
			
			else {
				if (expiryDate.after(existingShortUrl.getExpiryDateTime())) {
					log.info("Extending expiryDate of shortUrl={} for longUrl={} and businessUUID={}", existingShortUrl.getShortUrl(), existingShortUrl.getLongUrl(), existingShortUrl.getBusinessUUID());
					existingShortUrl.setExpiryDateTime(expiryDate);
				}
			}
			
			urlRepository.saveUrl(existingShortUrl);
			return new ShortUrl(existingShortUrl.getShortUrl(), existingShortUrl.getExpiryDateTime());
		}
		
		log.info("Creating a new shortUrl for longUrl={} and businessUUID={}", longUrl, businessUUID);
		
		long randomId;
		String shortUrlHash;

		do {
			randomId = urlServiceUtil.getRandomId(hashLength);

			shortUrlHash = urlServiceUtil.convertIdToHash(randomId, hashLength);

		} while (!urlServiceUtil.isHashValid(shortUrlHash));
		
		String shortUrl = shortUrlDomain + "/" + urlPrefix + "/" + shortUrlHash;
		
		Date currentDateTime = new Date();
		UrlDetails shortUrlDetails = new UrlDetails(shortUrlHash, longUrl, shortUrl, currentDateTime, expiryDate, businessUUID,
				additionalParams, currentDateTime, true);
		
		urlRepository.saveUrl(shortUrlDetails);
		
		return new ShortUrl(shortUrl, expiryDate);
		
	}
	
	/**
	 * Returns the HTML response for redirecting to the long URL from the short URL hash
	 * 
	 * @param shortUrlHash
	 * @return redirectingHtmlResponse
	 * @throws ShortUrlException
	 */
	public String getHtmlForRedirectingToLongUrl(String shortUrlHash) throws ShortUrlException {

		UrlDetails existingUrlDetails = getExistingShortUrlDetails(shortUrlHash);

		if (existingUrlDetails==null) {
			
			log.info("shortUrlHash={} is not present in DB ", shortUrlHash);
			throw new ShortUrlNotFoundException(UrlErrorCodes.SHORT_URL_NOT_FOUND);

		}

		if(existingUrlDetails.getExpiryDateTime().before(new Date())) {
			
			log.info(" Short Url for shortUrlHash={} is expired ", shortUrlHash);
			throw new ShortUrlNotFoundException(UrlErrorCodes.SHORT_URL_NOT_FOUND);

		}
		
		log.info("shortUrl for ShortUrlHash= {} redirects to longUrl= {}", shortUrlHash, existingUrlDetails.getLongUrl());

		return replaceUrlInRedirectingHtmlTemplate(existingUrlDetails.getLongUrl());
	}
	
	public UrlDetails getShortUrlDetails(String shortUrlHash) throws ShortUrlException {
		
		UrlDetails existingUrlDetails = shortUrlCacheAdapter.fetchUrlDetailsFromCache(shortUrlHash);
		
		if(existingUrlDetails != null) {
			return existingUrlDetails;
		}
		
		existingUrlDetails = getExistingShortUrlDetails(shortUrlHash);

		if (existingUrlDetails==null) {
			
			log.info("shortUrlHash={} is not present in DB ", shortUrlHash);
			throw new ShortUrlNotFoundException(UrlErrorCodes.SHORT_URL_NOT_FOUND);

		}

		if(existingUrlDetails.getExpiryDateTime().before(new Date())) {
			
			log.info(" Short Url for shortUrlHash={} is expired ", shortUrlHash);
			throw new ShortUrlNotFoundException(UrlErrorCodes.SHORT_URL_NOT_FOUND);

		}
		
		long ttl = urlServiceUtil.getDurationBetweenTwoDatesInSeconds(existingUrlDetails.getExpiryDateTime(), new Date());
		if(ttl > 24*60*60) {
			ttl = 24*60*60;
		}
		
		shortUrlCacheAdapter.saveInCache(shortUrlHash, existingUrlDetails, ttl);
		
		return existingUrlDetails;
	}
	
	/**
	 * Returns the HTML response redirecting to the longUrl
	 * @param longUrl
	 * @return HTML response redirecting to longUrl
	 */
	public String replaceUrlInRedirectingHtmlTemplate(String longUrl) {
		String html = "<!DOCTYPE html>\r\n<html>\r\n<head lang=\"en\">\r\n    <meta charset=\"UTF-8\">\r\n"
				+ "<meta http-equiv=\"cache-control\" content=\"max-age=0\" />\r\n"
				+ "<meta http-equiv=\"cache-control\" content=\"no-cache\" />\r\n"
				+ "<meta http-equiv=\"expires\" content=\"0\" />\r\n"
				+ "<meta http-equiv=\"expires\" content=\"Tue, 01 Jan 1980 1:00:00 GMT\" />\r\n"
				+ "<meta http-equiv=\"pragma\" content=\"no-cache\" />\r\n"
				+ "<meta http-equiv=\"refresh\" content=\"0; URL='_url'\" />\r\n    <title></title>\r\n</head>\r\n<body>\r\n_loading  Please Wait\r\n</body>\r\n</html>";
		html = html.replace("_url", longUrl);

		return html;
	}
	
	/**
	 * Fetches the urlDetails from the database using the shortUrlHash
	 * 
	 * @param shortUrlHash
	 * @return urlDetails
	 * @throws ShortUrlException
	 */
	private UrlDetails getExistingShortUrlDetails(String shortUrlHash) throws ShortUrlException {

		List<UrlDetails> existingShortUrls = urlRepository.getUrlDetailsByShortUrlHash(shortUrlHash);
		if(existingShortUrls.isEmpty()) {
			return null;
		}
		return existingShortUrls.get(0);
	}
	
}
