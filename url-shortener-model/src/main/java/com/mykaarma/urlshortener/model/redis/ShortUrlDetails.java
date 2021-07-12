package com.mykaarma.urlshortener.model.redis;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import com.mykaarma.urlshortener.model.jpa.ShortUrlAttributes;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@RedisHash("short-url-details")
public class ShortUrlDetails implements Serializable{
	
	@Id
	@Indexed
	private Long id;
	private Long version;
	private String shortUrl;
	private String eventCategory;
	private String eventLabel;
	private String eventAction;
	private Long eventValue;
	private String additionalParamsJson;
	private Date ttl;
	@TimeToLive
	private long expiryDuration;
	
	public ShortUrlDetails(ShortUrlAttributes shortUrlAttributes,long expiryDuration)
	{
		this.id=shortUrlAttributes.getId();
		this.version=shortUrlAttributes.getVersion();
		this.shortUrl=shortUrlAttributes.getShortUrl();
		this.eventCategory=shortUrlAttributes.getEventCategory();
		this.eventLabel=shortUrlAttributes.getEventLabel();
		this.eventAction=shortUrlAttributes.getEventAction();
		this.eventValue=shortUrlAttributes.getEventValue();
		this.additionalParamsJson=shortUrlAttributes.getAdditionalParamsJson();
		this.expiryDuration=expiryDuration;
		
		
		
	}
	
	
	
	
	
	
	
	
	

}
