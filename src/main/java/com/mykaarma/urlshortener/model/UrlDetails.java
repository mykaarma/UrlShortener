package com.mykaarma.urlshortener.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UrlDetails implements Serializable {
	
	private long secondaryId;
	private String longUrl;
	private String shortUrl;
	private Date creationDateTime;
	private Date expiryDateTime;
	private String businessId;
	private String eventAction;
	private String eventLabel;
	private String eventCategory;
	private Long eventValue;
	private Map<String, String> additionalParams;
}
