package com.mykaarma.urlshortener.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.mykaarma.urlshortener.exception.ShortUrlDuplicateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mykaarma.urlshortener.enums.UrlErrorCodes;
import com.mykaarma.urlshortener.exception.BadRedirectingRequestException;
import com.mykaarma.urlshortener.exception.BadShorteningRequestException;
import com.mykaarma.urlshortener.exception.ShortUrlException;
import com.mykaarma.urlshortener.exception.ShortUrlNotFoundException;
import com.mykaarma.urlshortener.model.AvailableHashPool;
import com.mykaarma.urlshortener.model.UrlDetails;
import com.mykaarma.urlshortener.persistence.AvailableHashPoolAdapter;
import com.mykaarma.urlshortener.persistence.ShortUrlCacheAdapter;
import com.mykaarma.urlshortener.persistence.ShortUrlDatabaseAdapter;
import com.mykaarma.urlshortener.util.UrlServiceUtil;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

@Service
@Slf4j
public class UrlService {
	
	private UrlServiceUtil urlServiceUtil;
	
	private ShortUrlDatabaseAdapter urlRepository;
	
	private ShortUrlCacheAdapter shortUrlCacheAdapter;
	
	private AvailableHashPoolAdapter availableHashPoolAdapter;
	
	@Autowired
	public UrlService(UrlServiceUtil urlServiceUtil, ShortUrlDatabaseAdapter urlRepository, ShortUrlCacheAdapter shortUrlCacheAdapter, AvailableHashPoolAdapter availableHashPoolAdapter) {
		
		this.urlServiceUtil = urlServiceUtil;
		this.urlRepository = urlRepository;
		this.shortUrlCacheAdapter = shortUrlCacheAdapter;
		this.availableHashPoolAdapter = availableHashPoolAdapter;
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
	 * @return UrlDetails
	 * @throws ShortUrlException
	 */
	public UrlDetails shortenUrl(String longUrl, String shortUrlDomain, long expiryDuration, String businessUUID, Map<String, String> additionalParams,
			boolean overwrite, String urlPrefix) throws ShortUrlException {

		if (longUrl == null || shortUrlDomain == null) {
			throw new BadShorteningRequestException(UrlErrorCodes.SHORT_URL_BAD_REQUEST, "longUrl or domain not present in request");
		}

		if (!urlServiceUtil.isUrlValid(longUrl)) {
			log.error(String.format("Url=%s is Malformed", longUrl));
			throw new BadShorteningRequestException(UrlErrorCodes.SHORT_URL_BAD_REQUEST, "Invalid longUrl provided in request");

		}

		if (!urlServiceUtil.isExpiryDurationValid(expiryDuration)) {
			log.error(String.format("Duration=%d is Invalid for businessUUID=%s , longUrl=%s", expiryDuration, businessUUID, longUrl));
			throw new BadShorteningRequestException(UrlErrorCodes.SHORT_URL_BAD_REQUEST, "Invalid expiryDuration provided in request");

		}

		Date expiryDate = urlServiceUtil.findExpiryDate(expiryDuration);

		UrlDetails existingShortUrl = urlRepository.getActiveUrlDetailsByLongUrlAndBusinessUUIDAndDomain(longUrl, businessUUID, shortUrlDomain);

		if (existingShortUrl != null) {

			log.info(String.format("ShortUrl=%s already present for longUrl=%s businessUUID=%s shortUrlDomain=%s", existingShortUrl.getShortUrl(), existingShortUrl.getLongUrl(), existingShortUrl.getBusinessUUID(), existingShortUrl.getShortUrlDomain()));

			if (overwrite) {
				if (expiryDate.before(existingShortUrl.getExpiryDateTime())) {
					log.error(String.format("The provided expiryDate for shortUrl=%s is before the existing expiryDate", existingShortUrl.getShortUrl()));
				}


				log.warn(String.format("overwriting existing shortUrl details of shortUrl = %s", existingShortUrl.getShortUrl()));
				existingShortUrl.setExpiryDateTime(expiryDate);
				existingShortUrl.setAdditionalParams(additionalParams);
			} else {
				if (expiryDate.after(existingShortUrl.getExpiryDateTime())) {
					log.info(String.format("Extending expiryDate of shortUrl=%s for longUrl=%s and businessUUID=%s", existingShortUrl.getShortUrl(), existingShortUrl.getLongUrl(), existingShortUrl.getBusinessUUID()));
					existingShortUrl.setExpiryDateTime(expiryDate);
				}
			}
			try {
				urlRepository.saveUrl(existingShortUrl);
			} catch (ShortUrlException e) {
				throw new ShortUrlException(UrlErrorCodes.SHORT_URL_INTERNAL_SERVER_ERROR, "Failed to update the url details in the database");
			}

			return existingShortUrl;
		}

		log.info(String.format("Creating a new shortUrl for longUrl=%s and businessUUID=%s", longUrl, businessUUID));
		int retryCount = 1;
		UrlDetails shortUrlDetails;
		shortUrlDetails = createShortUrl(shortUrlDomain, longUrl, expiryDate, businessUUID, additionalParams, urlPrefix, retryCount);

		return shortUrlDetails;

	}

	private UrlDetails createShortUrl(String shortUrlDomain, String longUrl, Date expiryDate, String businessUUID, Map<String, String> additionalParams,String urlPrefix,int retryCount) {

		log.info(String.format("Creating a new shortUrl using retry method  for longUrl=%s and businessUUID=%s", longUrl, businessUUID));

		AvailableHashPool hashPool = availableHashPoolAdapter.fetchAvailableShortUrlHash();
		if(hashPool == null) {
			log.error("All hashes have been exhausted. Generate new ones to keep the microservice running.");
			throw new ShortUrlException(UrlErrorCodes.HASHES_EXHAUSTED, "Unique hashes not available in pool");
		}
		String shortUrlHash = hashPool.getShortUrlHash();
		availableHashPoolAdapter.removeHashFromPool(shortUrlHash);

		UrlDetails shortUrlDetails = new UrlDetails(shortUrlHash, shortUrlDomain, longUrl, null, new Date(), expiryDate, businessUUID,
				additionalParams, new Date(), true);

		if(!shortUrlDomain.isEmpty() && shortUrlDomain.charAt(shortUrlDomain.length() - 1) != '/') {
			shortUrlDomain += '/';
		}
		if(urlPrefix != null && !urlPrefix.isEmpty() && urlPrefix.charAt(urlPrefix.length()-1)!='/') {
			urlPrefix += '/';
		}
		else{
			urlPrefix = "";
		}

		String shortUrl = shortUrlDomain + urlPrefix + shortUrlHash;

		shortUrlDetails.setShortUrl(shortUrl);

		try {
			urlRepository.saveUrl(shortUrlDetails);
		}
		catch (ShortUrlDuplicateException e)
		{
			log.info(String.format("DuplicateHashKeyException Exception Occurred, retrying creating short url for longUrl=%s businessUUID=%s domainPurpose=%s",longUrl, businessUUID, shortUrlDomain));
			if(retryCount ==6)
			{
				log.error("Failed to create shortUrl even after retries for long url={} with exception={}",longUrl,e);
				throw new ShortUrlException(UrlErrorCodes.SHORT_URL_INTERNAL_SERVER_ERROR, "Failed to Create ShortUrl");
			}
			else {
				shortUrlDetails = createShortUrl(shortUrlDomain, longUrl,expiryDate, businessUUID,additionalParams,urlPrefix,retryCount+1);
			}


		}
		catch(ShortUrlException e) {
			throw new ShortUrlException(UrlErrorCodes.SHORT_URL_INTERNAL_SERVER_ERROR, "Failed to save the url details to database");
		}

		return shortUrlDetails;
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
			throw new ShortUrlNotFoundException(UrlErrorCodes.SHORT_URL_NOT_FOUND, "Long URL not found for the given short URL");

		}

		if(existingUrlDetails.getExpiryDateTime().before(new Date())) {
			
			log.info(" Short Url for shortUrlHash={} is expired ", shortUrlHash);
			throw new ShortUrlNotFoundException(UrlErrorCodes.SHORT_URL_NOT_FOUND, "Long URL not found for the given short URL");

		}
		
		log.info(String.format("shortUrl for shortUrlHash=%s redirects to longUrl=%s", shortUrlHash, existingUrlDetails.getLongUrl()));

		return replaceUrlInRedirectingHtmlTemplate(existingUrlDetails.getLongUrl());
	}
	
	public UrlDetails getShortUrlDetails(String shortUrlHash) throws ShortUrlException {
		
		UrlDetails existingUrlDetails = shortUrlCacheAdapter.fetchUrlDetailsFromCache(shortUrlHash);
		
		if(existingUrlDetails != null) {
			log.info("Fetching existing short url details from cache for shortUrlHash="+shortUrlHash);
			return existingUrlDetails;
		}
		
		existingUrlDetails = getExistingShortUrlDetails(shortUrlHash);

		if (existingUrlDetails==null) {
			
			log.info(String.format("shortUrlHash=%s is not present in DB ", shortUrlHash));
			throw new ShortUrlNotFoundException(UrlErrorCodes.SHORT_URL_NOT_FOUND, "Long URL not found for the given short URL");

		}

		if(existingUrlDetails.getExpiryDateTime().before(new Date())) {
			
			log.info(String.format("Short Url for shortUrlHash=%s is expired ", shortUrlHash));
			throw new ShortUrlNotFoundException(UrlErrorCodes.SHORT_URL_NOT_FOUND, "Long URL not found for the given short URL");

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

		UrlDetails existingShortUrl = urlRepository.getUrlDetailsByShortUrlHash(shortUrlHash);
		return existingShortUrl;
	}
	
}
