package com.mykaarma.urlshortener.service;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mykaarma.googleanalytics.EventHit;
import com.mykaarma.googleanalytics.GoogleAnalytics;
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
	 * Creating a shortUrl for the provided longUrl and businessId
	 * 
	 * @param longUrl
	 * @param shortUrlDomain
	 * @param expiryDuration
	 * @param businessId
	 * @param eventAction
	 * @param eventLabel
	 * @param eventCategory
	 * @param eventValue
	 * @param additionalParams
	 * @param overwrite
	 * @param hashLength
	 * @param blackListedWordsFileUrl
	 * @param randomAlphabet
	 * @param urlPrefix
	 * @return ShortUrl
	 * @throws ShortUrlException
	 */
	public ShortUrl shortenUrl(String longUrl, String shortUrlDomain, long expiryDuration, String businessId, String eventAction, 
			String eventLabel, String eventCategory, Long eventValue, Map<String, String> additionalParams, boolean overwrite, int hashLength,
			String blackListedWordsFileUrl, String randomAlphabet, String urlPrefix) throws ShortUrlException {
		
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
			log.error("Duration={} is Invalid for businessId={} , longUrl={}", expiryDuration, businessId, longUrl);
			throw new BadShorteningRequestException(UrlErrorCodes.SHORT_URL_BAD_REQUEST);

		}
		
		Date expiryDate = urlServiceUtil.findExpiryDate(expiryDuration);
		
		UrlDetails existingShortUrl = urlRepository.getShortUrlByLongUrlAndBusinessId(longUrl, businessId);
		
		if (existingShortUrl!=null) {
			
			log.info("ShortUrl={} already present for longUrl={} and businessId={}", existingShortUrl.getShortUrl(), existingShortUrl.getLongUrl(), existingShortUrl.getBusinessId());
			
			if(overwrite) {
				if (expiryDate.before(existingShortUrl.getExpiryDateTime())) {
					log.error("The provided expiryDate for the shortUrl = {} is before the existing expiryDate", existingShortUrl.getShortUrl());
				}
				
				if((eventAction==null || eventCategory==null) && (existingShortUrl.getEventAction()!=null && existingShortUrl.getEventCategory()!=null)) {
					log.info("Disabling tracking of shortUrl={} for longUrl={} and businessId={}", existingShortUrl.getShortUrl(), existingShortUrl.getLongUrl(), existingShortUrl.getBusinessId());
				}
				
				log.warn("overwriting existing shortUrl details of shortUrl = {}", existingShortUrl.getShortUrl());
				existingShortUrl.setExpiryDateTime(expiryDate);
				existingShortUrl.setEventAction(eventAction);
				existingShortUrl.setEventCategory(eventCategory);
				existingShortUrl.setEventLabel(eventLabel);
				existingShortUrl.setEventValue(eventValue);
				existingShortUrl.setAdditionalParams(additionalParams);
			}
			
			else {
				if (expiryDate.after(existingShortUrl.getExpiryDateTime())) {
					log.info("Extending expiryDate of shortUrl={} for longUrl={} and businessId={}", existingShortUrl.getShortUrl(), existingShortUrl.getLongUrl(), existingShortUrl.getBusinessId());
					existingShortUrl.setExpiryDateTime(expiryDate);
				}
				if((eventAction!=null && eventCategory!=null) && (existingShortUrl.getEventAction()==null || existingShortUrl.getEventCategory()==null)) {
					log.info("Enabling tracking of shortUrl={} for longUrl={} and businessId={}", existingShortUrl.getShortUrl(), existingShortUrl.getLongUrl(), existingShortUrl.getBusinessId());
					existingShortUrl.setEventAction(eventAction);
					existingShortUrl.setEventCategory(eventCategory);
					existingShortUrl.setEventLabel(eventLabel);
					existingShortUrl.setEventValue(eventValue);
				}
			}
			
			return new ShortUrl(existingShortUrl.getShortUrl(), existingShortUrl.getExpiryDateTime());
		}
		
		log.info("Creating a new shortUrl for longUrl={} and businessId={}", longUrl, businessId);
		
		long secondaryId;
		String shortUrlHash;

		do {
			secondaryId = urlServiceUtil.getRandomId(hashLength);

			shortUrlHash = urlServiceUtil.convertIdToHash(secondaryId, hashLength);

		} while (!urlServiceUtil.isHashValid(shortUrlHash));
		
		String shortUrl = shortUrlDomain + "/" + urlPrefix + "/" + shortUrlHash;
		
		UrlDetails shortUrlDetails = new UrlDetails(secondaryId, longUrl, shortUrl, new Date(), expiryDate, businessId, eventAction, eventLabel, 
				eventCategory, eventValue, additionalParams);
		
		urlRepository.saveUrl(shortUrlDetails);
		
		return new ShortUrl(shortUrl, expiryDate);
		
	}
	
	/**
	 * Returns the HTML response for redirecting to the long URL from the short URL hash
	 * 
	 * @param shortUrlHash
	 * @param serviceAnalyticId
	 * @return redirectingHtmlResponse
	 * @throws ShortUrlException
	 */
	public String getHtmlForRedirectingToLongUrl(String shortUrlHash, String serviceAnalyticId) throws ShortUrlException {
		
		if (!urlServiceUtil.isHashValid(shortUrlHash)) {
			log.info("Invalid Hash={}  ", hashCode());
			throw new BadRedirectingRequestException(UrlErrorCodes.INVALID_SHORT_URL);
		}

		long id = urlServiceUtil.convertHashToId(shortUrlHash);

		UrlDetails existingLongUrl = urlRepository.getLongUrlBySecondaryId(id);

		if (existingLongUrl==null) {
			
			log.info("shortUrlHash={} is not present in DB ", shortUrlHash);
			throw new ShortUrlNotFoundException(UrlErrorCodes.SHORT_URL_NOT_FOUND);

		}

		if(existingLongUrl.getExpiryDateTime().before(new Date())) {
			
			log.info(" Short Url for shortUrlHash={} is expired ", shortUrlHash);
			throw new ShortUrlNotFoundException(UrlErrorCodes.SHORT_URL_NOT_FOUND);

		}
		
		if(existingLongUrl.getEventAction()==null || existingLongUrl.getEventCategory()==null) {
			log.info("shortUrl for ShortUrlHash= {} redirects to longUrl= {}", shortUrlHash, existingLongUrl.getLongUrl());

			return urlServiceUtil.replaceUrlInRedirectingHtmlTemplate(existingLongUrl.getLongUrl());
		}

		trackShortUrl(existingLongUrl, new Date(), serviceAnalyticId);
		
		log.info("shortUrl for ShortUrlHash= {} redirects to longUrl= {}", shortUrlHash, existingLongUrl.getLongUrl());

		return urlServiceUtil.replaceUrlInRedirectingHtmlTemplate(existingLongUrl.getLongUrl());
	}
	
	/**
	 * Returns the HTML response for redirecting to the long URL from the short URL hash. This does not require the service analytic ID for tracking
	 * 
	 * @param shortUrlHash
	 * @return redirectingHtmlResponse
	 * @throws ShortUrlException
	 */
	public String getHtmlForRedirectingToLongUrl(String shortUrlHash) throws ShortUrlException {
		
		return getHtmlForRedirectingToLongUrl(shortUrlHash, null);
	}
	
	/**
	 * Tracks the short URL
	 * 
	 * @param urlDetails
	 * @param logTime
	 * @param serviceAnalyticId
	 * @throws ShortUrlException
	 */
	private void trackShortUrl(UrlDetails urlDetails, Date logTime, String serviceAnalyticId) throws ShortUrlException {

		String eventCategory = urlDetails.getEventCategory();
		String eventAction = urlDetails.getEventAction();
		String eventLabel = urlDetails.getEventLabel();
		String shortUrl = urlDetails.getShortUrl();

		Long eventValue = urlDetails.getEventValue();
		Date expiryDateTime = urlDetails.getExpiryDateTime();

		log.info("short_url_extracted=\"" + shortUrl + "\"");

		log.info("log time : " + logTime);
		String logline = "SHORT URL OPENED short_url_opened=\"" + shortUrl + "\" ";

		if (expiryDateTime.after(logTime)) {

			logline += postToGoogleAnalytics(eventAction, eventCategory, eventLabel, eventValue, serviceAnalyticId);

			Map<String, String> additionalParams = urlDetails.getAdditionalParams();

			if (additionalParams == null) {
				log.info(logline);
				return;
			}
			for (Map.Entry<String, String> entry : additionalParams.entrySet()) {
				logline = logline + entry.getKey() + "=\"" + entry.getValue() + "\" ";
			}

		}
		log.info(logline);

	}
	
	/**
	 * Posts the event to Google Analytics
	 * 
	 * @param eventAction
	 * @param eventCategory
	 * @param eventLabel
	 * @param eventValue
	 * @param serviceAnalyticId
	 * @return logline
	 */
	private String postToGoogleAnalytics(String eventAction, String eventCategory, String eventLabel, Long eventValue, String serviceAnalyticId) {
		String logline = "";
		if (!(eventCategory == null || eventCategory.isEmpty() || eventAction == null
				|| eventAction.trim().isEmpty())) {
			eventLabel = eventLabel == null ? "" : eventLabel;
			eventValue = eventValue == null ? 1 : eventValue;

			String gaTrackingID = serviceAnalyticId;
			GoogleAnalytics ga = new GoogleAnalytics(gaTrackingID);
			EventHit event = new EventHit(eventCategory, eventAction, eventLabel, eventValue.intValue());
			event.clientId("SHORTURL");

			log.info("Posting event to GA. ga_tracking_id={} ga_event={}", gaTrackingID, event.toString());

			ga.post(event);

			logline += "event_category=\"" + eventCategory + "\" ";
			logline += "event_action=\"" + eventAction + "\" ";
			logline += "event_label=\"" + eventLabel + "\" ";
			logline += "event_value=\"" + eventValue + "\" ";
		} 
		else {
			logline += " ShortURL TTL expired. Not logging anything in GA. ";
		}
		return logline;
	}
}
