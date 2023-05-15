package com.mykaarma.urlshortener.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UrlDetails implements Serializable {
	
	private String shortUrlHash;
	private String shortUrlDomain;
	private String longUrl;
	private String shortUrl;
	private Date creationDateTime;
	private Date expiryDateTime;
	private String businessUUID;
	private Map<String, String> additionalParams;
	private Date lastAccessedTimestamp;
	private boolean isValid;
}
