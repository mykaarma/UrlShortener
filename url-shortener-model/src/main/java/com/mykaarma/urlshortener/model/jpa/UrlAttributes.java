package com.mykaarma.urlshortener.model.jpa;


import java.io.Serializable;
import java.util.Date;

import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
@Document(collection = "URL")
public class UrlAttributes implements Serializable {
	
	@Id
	private long secondaryId;
	@Indexed
	private String longUrl;
	private String shortUrl;
	private Date creationDateTime;
	private Date expiryDateTime;
	private long clickCount;
	private String businessId;
	
	private boolean isTrackingEnabled;
	

	public UrlAttributes() {
		super();

	}
	public void incrementClickCount()
	{
		this.clickCount++;
		
	}
	
	
	


}
