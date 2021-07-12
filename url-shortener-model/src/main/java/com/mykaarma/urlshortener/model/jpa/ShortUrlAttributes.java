package com.mykaarma.urlshortener.model.jpa;



import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mykaarma.urlshortener.model.redis.ShortUrlDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ShortUrlAttributes")
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class ShortUrlAttributes implements Serializable {
	@Id
	private Long id;
	private Long version;
	private String shortUrl;
	private String eventCategory;
	private String eventLabel;
	private String eventAction;
	private Long eventValue;
	private String additionalParamsJson;
	private Date ttl;
	
	public ShortUrlAttributes(ShortUrlDetails shortUrlDetails)
	{
		this.id=shortUrlDetails.getId();
		this.version=shortUrlDetails.getVersion();
		this.shortUrl=shortUrlDetails.getShortUrl();
		this.eventCategory=shortUrlDetails.getEventCategory();
		this.eventLabel=shortUrlDetails.getEventLabel();
		this.eventAction=shortUrlDetails.getEventAction();
		this.eventValue=shortUrlDetails.getEventValue();
		this.ttl=shortUrlDetails.getTtl();
		this.additionalParamsJson=shortUrlDetails.getAdditionalParamsJson();
		
		
		
	}

}
