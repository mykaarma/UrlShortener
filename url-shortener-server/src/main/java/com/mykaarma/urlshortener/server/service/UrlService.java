package com.mykaarma.urlshortener.server.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mykaarma.googleanalytics.EventHit;
import com.mykaarma.googleanalytics.GoogleAnalytics;
import com.mykaarma.urlshortener.model.dto.request.ShortenUrlRequestDTO;
import com.mykaarma.urlshortener.model.dto.response.ShortenUrlResponseDTO;
import com.mykaarma.urlshortener.model.enums.UrlErrorCodes;
import com.mykaarma.urlshortener.model.exception.BadClickCountRequestException;
import com.mykaarma.urlshortener.model.exception.BadRedirectingRequestException;
import com.mykaarma.urlshortener.model.exception.BadShorteningRequestException;
import com.mykaarma.urlshortener.model.exception.NoShortUrlForClickCountRequestException;
import com.mykaarma.urlshortener.model.exception.NoSuchElementFoundException;
import com.mykaarma.urlshortener.model.jpa.ShortUrlAttributes;
import com.mykaarma.urlshortener.model.jpa.UrlAttributes;
import com.mykaarma.urlshortener.model.redis.ShortUrlDetails;
import com.mykaarma.urlshortener.model.redis.UrlDetails;
import com.mykaarma.urlshortener.model.utils.RestURIConstants;
import com.mykaarma.urlshortener.server.repository.ShortUrlAttributesRepository;
import com.mykaarma.urlshortener.server.repository.ShortUrlDetailsRepository;
import com.mykaarma.urlshortener.server.repository.UrlDetailsRepository;
import com.mykaarma.urlshortener.server.repository.UrlRepository;
import com.mykaarma.urlshortener.server.util.UrlServiceUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UrlService {

	private UrlServiceUtil urlServiceUtil;
	private UrlRepository repository;
	private ShortUrlAttributesRepository shortUrlAttributesRepository;
	private UrlDetailsRepository urlDetailsRepository ;
	private ShortUrlDetailsRepository shortUrlDetailsRepository;
	

	 @Value("${service_analytic_id}")
	private String serviceAnalyticId ;

	public UrlService() {
		super();
	}

	@Autowired
	public UrlService(UrlServiceUtil urlServiceUtil, UrlRepository repository,
			ShortUrlAttributesRepository shortUrlAttributesRepository, UrlDetailsRepository urlDetailsRepository,ShortUrlDetailsRepository shortUrlDetailsRepository ) {
		super();
		this.urlServiceUtil = urlServiceUtil;
		this.repository = repository;
		this.shortUrlAttributesRepository = shortUrlAttributesRepository;
		
		this.urlDetailsRepository=urlDetailsRepository;
		this.shortUrlDetailsRepository=shortUrlDetailsRepository;
	}

	/**
	 * returns the shortened Url for a Long url
	 * 
	 * @param getShortUrlRequestDTO
	 *
	 * @return ShortenUrlResponseDTO
	 */
	public ShortenUrlResponseDTO shortenUrl(ShortenUrlRequestDTO getShortUrlRequestDTO) throws Exception {

		if (getShortUrlRequestDTO == null || getShortUrlRequestDTO.getExpiryDuration() == null
				|| getShortUrlRequestDTO.getLongUrl() == null || getShortUrlRequestDTO.getShortUrlDomain() == null) {

			throw new BadShorteningRequestException(UrlErrorCodes.BAD_REQUEST);

		}
		String longUrl = getShortUrlRequestDTO.getLongUrl();
		if (!urlServiceUtil.isUrlValid(longUrl)) {
			log.error("Url={} is Malformed", longUrl);
			throw new BadShorteningRequestException(UrlErrorCodes.BAD_REQUEST);

		}

		String businessId = getShortUrlRequestDTO.getBusinessId();
		String expiryDuration = getShortUrlRequestDTO.getExpiryDuration();

		if (!urlServiceUtil.isDurationValid(getShortUrlRequestDTO.getExpiryDuration())) {
			log.error("Duration={} is Invalid for businessId={} , longUrl={}", expiryDuration, businessId, longUrl);
			throw new BadShorteningRequestException(UrlErrorCodes.BAD_REQUEST);

		}
		long expiryDurationInSeconds=urlServiceUtil.findExpiryDurationInSeconds(expiryDuration);
		Date expiryDate = urlServiceUtil.findExpiryDate(getShortUrlRequestDTO.getExpiryDuration());
			///take from redis
		List<UrlDetails> sameLongUrlList = urlDetailsRepository.findByLongUrl(longUrl);

		if (sameLongUrlList.isEmpty()) {
			return createNewUrlEntity(getShortUrlRequestDTO, expiryDate,expiryDurationInSeconds);
		}
		UrlDetails sameUrlDetail = sameLongUrlList.get(0);
		
		if(expiryDate.before(sameUrlDetail.getExpiryDateTime()))
		{
			if (getShortUrlRequestDTO.isTrackingEnabled()&&!sameUrlDetail.isTrackingEnabled()) {
				
				enableTracking(getShortUrlRequestDTO, sameUrlDetail.getSecondaryId(), sameUrlDetail.getShortUrl());
			}

			return new ShortenUrlResponseDTO(sameUrlDetail.getShortUrl());
			
		}
		
			log.info("Short Url={} already present for longUrl={}", sameLongUrlList.get(0).getShortUrl(), longUrl);
			sameUrlDetail.setExpiryDateTime(expiryDate);
			
			sameUrlDetail.setExpiryDuration(expiryDurationInSeconds);
			sameUrlDetail.setBusinessId(businessId);
			
			List<UrlAttributes> urlAttributes= repository.findBySecondaryId(sameUrlDetail.getSecondaryId());
			
			UrlAttributes sameUrlAttribute=urlAttributes.get(0);
			
			sameUrlAttribute.setExpiryDateTime(expiryDate);
			sameUrlAttribute.setBusinessId(businessId);
			
			
			
			if(sameUrlAttribute.isTrackingEnabled())
			{
		Optional<ShortUrlDetails> shortUrlDetails=shortUrlDetailsRepository.findById(sameUrlDetail.getSecondaryId());
				
				if(shortUrlDetails.isPresent())
				{
					shortUrlDetails.get().setTtl(expiryDate);  
					shortUrlDetails.get().setExpiryDuration(expiryDurationInSeconds);
					
					List<ShortUrlAttributes>shortUrlAttributes=shortUrlAttributesRepository.findById(sameUrlDetail.getSecondaryId());
					
					if(shortUrlAttributes!=null&&!shortUrlAttributes.isEmpty())
					{
						shortUrlAttributes.get(0).setTtl(expiryDate);
						
						shortUrlAttributesRepository.save(shortUrlAttributes.get(0));
					}
					
					
					shortUrlDetailsRepository.save(shortUrlDetails.get());
					
				}
				
			}
			

			log.info("Extending the Expiry Date of shortUrl={} for longUrl ={} on request of BusinessId={}",
					sameUrlAttribute.getShortUrl(), longUrl, sameUrlAttribute.getBusinessId());
			
			
			
		
			
			

			urlDetailsRepository.save(sameUrlDetail);
			repository.save(sameUrlAttribute);
			
		
		
		

	
		if (getShortUrlRequestDTO.isTrackingEnabled()&&!sameUrlDetail.isTrackingEnabled()) {
			
			enableTracking(getShortUrlRequestDTO, sameUrlDetail.getSecondaryId(), sameUrlDetail.getShortUrl());
		}

		return new ShortenUrlResponseDTO(sameUrlDetail.getShortUrl());

	}

	/**
	 * Creates a new Record for shortUrl-LongUrl Mapping in DB and returns shortUrl;
	 * 
	 * @param getShortUrlRequestDTO
	 * @param expiryDate
	 * 
	 * 
	 * @return ShortenUrlResponseDTO
	 */
	public ShortenUrlResponseDTO createNewUrlEntity(ShortenUrlRequestDTO getShortUrlRequestDTO, Date expiryDate,long  expiryDurationInSeconds) {

		String businessId = getShortUrlRequestDTO.getBusinessId();
		String longUrl = getShortUrlRequestDTO.getLongUrl();
		log.info("Creating a new ShortUrl for longUrl={},businessId={}", longUrl, businessId);
		long id;
		String hash;

		do {
			id = urlServiceUtil.getRandomId();

			hash = urlServiceUtil.convertToHash(id);

		} while (!urlServiceUtil.isValid(hash));

		String shortUrl = getShortUrlRequestDTO.getShortUrlDomain() + "/"+RestURIConstants.REDIRECT +"/"+ hash;

		log.info("Created  a new ShortUrl={} for longUrl={},businessId={}", shortUrl, longUrl, businessId);

		
		
		UrlAttributes urlAttribute = new UrlAttributes(id, getShortUrlRequestDTO.getLongUrl(), shortUrl, new Date(),
				expiryDate, 0L, getShortUrlRequestDTO.getBusinessId(),getShortUrlRequestDTO.isTrackingEnabled());

		repository.save(urlAttribute);
		
		UrlDetails urlDetails=new UrlDetails(id, getShortUrlRequestDTO.getLongUrl(), shortUrl,	expiryDurationInSeconds,expiryDate, 0L, getShortUrlRequestDTO.getBusinessId(),getShortUrlRequestDTO.isTrackingEnabled());
		
		
		urlDetailsRepository.save(urlDetails);

		if (getShortUrlRequestDTO.isTrackingEnabled()) {
			enableTracking(getShortUrlRequestDTO, id, shortUrl);
		}

		return new ShortenUrlResponseDTO(shortUrl);

	}

	/**
	 * Finds the ShortUrl in the DB and returns Corresponding Long Url
	 * 
	 * @param scheme
	 * @param shortUrlDomain
	 * @param shortUrlHash
	 * 
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String findLongUrl(String shortUrlHash) throws Exception {
		

		if (!urlServiceUtil.isValid(shortUrlHash)) {
			log.info("Invalid Hash={}  ", hashCode());
			throw new BadRedirectingRequestException(UrlErrorCodes.INVALID_SHORT_URL);
		}

		long id = urlServiceUtil.convertHashToId(shortUrlHash);

		List<UrlDetails> sameIdList = urlDetailsRepository.findBySecondaryId(id);

		if (sameIdList.isEmpty()) {
			log.info("shortUrlHash={} is not present in DB ", shortUrlHash);
			throw new NoSuchElementFoundException(UrlErrorCodes.SHORT_URL_NOT_FOUND);

		}

		if (sameIdList.get(0).getExpiryDateTime().before(new Date())) {
			log.info(" Short Url for shortUrlHash={} is expired ", shortUrlHash);

			throw new NoSuchElementFoundException(UrlErrorCodes.SHORT_URL_NOT_FOUND);

		}
		
		urlServiceUtil.incrementClickCount(sameIdList.get(0));
		


		
		if(!sameIdList.get(0).isTrackingEnabled()) {
			log.info("shortUrl for ShortUrlHash= {} redirects to longUrl= {}", shortUrlHash, sameIdList.get(0).getLongUrl());

			return sameIdList.get(0).getLongUrl();
		}

		Optional<ShortUrlDetails> shortUrlDetails = shortUrlDetailsRepository.findById(id);
		if ( shortUrlDetails.isPresent()) {
			trackShortUrl(shortUrlDetails.get(), new Date());

		}

		log.info("shortUrl for ShortUrlHash= {} redirects to longUrl= {}", shortUrlHash, sameIdList.get(0).getLongUrl());

		return sameIdList.get(0).getLongUrl();

	}



	/**
	 * @param longUrl
	 * @return html code for Redirecting to Long Url(string)
	 */
	public String getHtmlForRedirectingToLongUrl(String longUrl) {
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
	 * Creating  ShortUrlAttributes for a shortUrl 
	 * 
	 * @param shortenUrlRequestDTO
	 * @param id
	 *
	 * @param shortUrl
	 */
	public void enableTracking(ShortenUrlRequestDTO shortenUrlRequestDTO, long id, String shortUrl) {

		String expiryDuration = shortenUrlRequestDTO.getExpiryDuration();

		String eventAction = shortenUrlRequestDTO.getEventAction();
		String eventLabel = shortenUrlRequestDTO.getEventLabel();
		String eventCategory = shortenUrlRequestDTO.getEventCategory();
		long eventValue = shortenUrlRequestDTO.getEventValue();
		Map<String, String> additionalParams = shortenUrlRequestDTO.getAdditionalParams();

		Date expiryDate=urlServiceUtil.findExpiryDate(expiryDuration);
		ShortUrlAttributes shortUrlAttributes = null;

		String shortUrlWithoutDomain = shortUrl;
		int index = shortUrlWithoutDomain.lastIndexOf("/");
		if (index >= 0) {
			shortUrlWithoutDomain = shortUrlWithoutDomain.substring(index + 1);
		}

		Optional<ShortUrlDetails> shortUrlDetails = shortUrlDetailsRepository.findById(id);

		if (shortUrlDetails.isPresent()) {
			shortUrlAttributes = new ShortUrlAttributes(shortUrlDetails.get());
		}

		if (eventCategory == null || eventCategory.isEmpty() || eventAction == null || eventAction.trim().isEmpty()) {

			shortUrlAttributes = getNonTrackableAttributes(shortUrlAttributes, shortenUrlRequestDTO, id, shortUrl);
		} else {

			if (shortUrlAttributes == null) {
				shortUrlAttributes = new ShortUrlAttributes();
			}
			shortUrlAttributes.setId(id);
			shortUrlAttributes.setEventCategory(eventCategory);
			shortUrlAttributes.setEventAction(eventAction);
			shortUrlAttributes.setEventLabel(eventLabel);
			shortUrlAttributes.setEventValue(eventValue);
			shortUrlAttributes.setTtl(expiryDate); // setting it to current time
																						// PLUS url expiry duration

			String additionalParamsJson = null;
			if (additionalParams != null && !additionalParams.isEmpty()) {
				try {
					additionalParamsJson = new ObjectMapper().writeValueAsString(additionalParams);
				} catch (JsonProcessingException e) {
					additionalParamsJson = null;
				}
			}
			shortUrlAttributes.setAdditionalParamsJson(additionalParamsJson);
		}

		if (shortUrlAttributes != null) {
			shortUrlAttributes.setShortUrl(shortUrlWithoutDomain);
			
			ShortUrlDetails shortUrlDetail=new ShortUrlDetails(shortUrlAttributes,urlServiceUtil.findExpiryDurationInSeconds(expiryDuration));
			
			shortUrlDetailsRepository.save(shortUrlDetail);
			shortUrlAttributesRepository.save(shortUrlAttributes);
		}

	}

	/**
	 * Returns ShortUrlAttributes for a shortUrl which cant be Tracked in GA
	 * 
	 * @param shortUrlAttributes
	 * @param shortenUrlRequestDTO
	 * @param id
	 * @param shortUrl
	 *
	 * @return ShortUrlAttributes
	 */
	public ShortUrlAttributes getNonTrackableAttributes(ShortUrlAttributes shortUrlAttributes,
			ShortenUrlRequestDTO shortenUrlRequestDTO, long id, String shortUrl) {
		String longUrl = shortenUrlRequestDTO.getLongUrl();
		String expiryDuration = shortenUrlRequestDTO.getExpiryDuration();
		String businessId = shortenUrlRequestDTO.getBusinessId();
		String eventAction = shortenUrlRequestDTO.getEventAction();
		String eventCategory = shortenUrlRequestDTO.getEventCategory();
		Map<String, String> additionalParams = shortenUrlRequestDTO.getAdditionalParams();

		log.error(
				"No eventCategory or eventAction present for google analytics logging. WILL NOT BE TRACKED IN GA. dealer_Uuid="
						+ businessId + " long_url=" + longUrl + " short_url=" + shortUrl + " event_category=\""
						+ eventCategory + "\" event_action=\"" + eventAction + "\"");

		if (additionalParams != null && !additionalParams.isEmpty()) {
			String additionalParamsJson = null;
			try {
				additionalParamsJson = new ObjectMapper().writeValueAsString(additionalParams);
			} catch (JsonProcessingException e) {
				additionalParamsJson = null;
			}
			if (additionalParamsJson != null) {
				if (shortUrlAttributes == null) {
					shortUrlAttributes = new ShortUrlAttributes();
					shortUrlAttributes.setId(id);
				}

				shortUrlAttributes.setEventCategory(null);
				shortUrlAttributes.setEventAction(null);
				shortUrlAttributes.setEventLabel(null);
				shortUrlAttributes.setEventValue(null);
				shortUrlAttributes.setAdditionalParamsJson(additionalParamsJson);
				shortUrlAttributes.setTtl(urlServiceUtil.findExpiryDate(expiryDuration));
			}
		}
		return shortUrlAttributes;

	}

	/**
	 * It Tracks the ShortUrl and Post the Details on GA when a shortUrl is hit
	 * 
	 * @param shortUrlAttributes
	 *
	 * @param logTime
	 */
	public void trackShortUrl(ShortUrlDetails shortUrlDetails, Date logTime) {

		String eventCategory = shortUrlDetails.getEventCategory();
		String eventAction = shortUrlDetails.getEventAction();
		String eventLabel = shortUrlDetails.getEventLabel();
		String shortUrl = shortUrlDetails.getShortUrl();

		Long eventValue = shortUrlDetails.getEventValue();
		Date ttl = shortUrlDetails.getTtl();

		log.info("short_url_extracted=\"" + shortUrl + "\"");

		log.info("log time : " + logTime);
		String logline = "SHORT URL OPENED short_url_opened=\"" + shortUrl + "\" ";

		if (ttl.after(logTime)) {

			logline += postToGoogleAnalytics(eventAction, eventCategory, eventLabel, eventValue);

			HashMap<String, String> additionalParams = null;
			if (shortUrlDetails.getAdditionalParamsJson() != null
					&& !shortUrlDetails.getAdditionalParamsJson().trim().isEmpty()) {
				try {
					additionalParams = new ObjectMapper().readValue(shortUrlDetails.getAdditionalParamsJson(),
							HashMap.class);
				} catch (Exception e) {
					additionalParams = null;
				}
			}

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
	 * It posts All the details about the ShortUrlAttributes to the Google Analytics
	 * 
	 * @param eventAction
	 * @param eventCategory
	 * @param eventLabel
	 * @param eventValue
	 * 
	 *
	 * @return
	 */
	String postToGoogleAnalytics(String eventAction, String eventCategory, String eventLabel, Long eventValue) {
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
		} else {
			logline += " ShortURL TTL expired. Not logging anything in GA. ";
		}
		return logline;
	}
	
	
	 /**
		 * @param scheme
		 * @param shortUrlDomain
		 * @param shorturlHash
		 * @return clickCount (long)
		 * @throws Exception
		 */
		public long findClickCounts(String shorturlHash) throws Exception
		 {
			 
			boolean isShortUrlHashValid=true;
		
			 isShortUrlHashValid=urlServiceUtil.isValid(shorturlHash);
			 
			
			 
			 if(!isShortUrlHashValid)
			 {
				 log.info("shortUrl hash={} is invalid",shorturlHash);
				 
				 throw new BadClickCountRequestException(UrlErrorCodes.INVALID_SHORT_URL);
				 
			 }
			 
			 
			 
			 long id=urlServiceUtil.convertHashToId(shorturlHash);
			
			 List<UrlDetails>sameIdList=urlDetailsRepository.findBySecondaryId(id);
			 
			 if(sameIdList.isEmpty())
			 {
				 
				 log.info("shortUrl hash={} is not Found",shorturlHash);
				 throw new NoShortUrlForClickCountRequestException(UrlErrorCodes.SHORT_URL_NOT_FOUND);
				 
			 }
			 
			
			 log.info("Click Count for shortUrl with hash={} is {}",shorturlHash,sameIdList.get(0).getClickCount());
			 return sameIdList.get(0).getClickCount();
			 
			 
			 
			 
			 
		 }
	
	
	
	

}
