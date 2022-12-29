package com.mykaarma.urlshortener.exception;

import com.mykaarma.urlshortener.enums.UrlErrorCodes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadShorteningRequestException extends ShortUrlException{
private UrlErrorCodes urlErrorCodes;
	
	public BadShorteningRequestException(UrlErrorCodes urlErrorCodes)
	{
		this.urlErrorCodes=urlErrorCodes;
	}

}
