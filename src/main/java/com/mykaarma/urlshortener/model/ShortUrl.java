package com.mykaarma.urlshortener.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ShortUrl {
	
	private String shortUrl;
	private Date expiryDate;
}
