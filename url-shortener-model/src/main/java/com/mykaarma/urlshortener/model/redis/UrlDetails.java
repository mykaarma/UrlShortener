package com.mykaarma.urlshortener.model.redis;

import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@RedisHash("url-details")
public class UrlDetails implements Serializable{
	
	
	@Indexed
	@Id
	private long secondaryId;
	
	@Indexed
	private String longUrl;
	
	private String shortUrl;
	
	
	@TimeToLive
	private long expiryDuration;
	
	private Date expiryDateTime;
	
	
	private long clickCount;
	private String businessId;
	private boolean isTrackingEnabled;
	
	
	public void incrementClickCount()
	{
		this.clickCount++;
		
	}
	
	
	
	
	
	
	
	
	

}
