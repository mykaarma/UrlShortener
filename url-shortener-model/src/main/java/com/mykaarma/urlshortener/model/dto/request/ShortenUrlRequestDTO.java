package com.mykaarma.urlshortener.model.dto.request;



import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
@Data
public class ShortenUrlRequestDTO implements Serializable{
	
	private String longUrl;
	private String shortUrlDomain;
	private  String expiryDuration ; //year:month:day:hour:sec
	private String businessId;
	private String eventAction ;
	private String eventLabel ;
	private String	eventCategory;
	private long eventValue ;
	private Map<String, String>	additionalParams ;
	boolean isTrackingEnabled;
	
	
	public ShortenUrlRequestDTO() {
		super();

	}


}
