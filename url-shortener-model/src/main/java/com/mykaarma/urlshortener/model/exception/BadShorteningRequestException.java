package com.mykaarma.urlshortener.model.exception;

import com.mykaarma.urlshortener.model.enums.UrlErrorCodes;


import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class BadShorteningRequestException extends Exception{
	private UrlErrorCodes urlErrorCodes;
	
	public BadShorteningRequestException(UrlErrorCodes urlErrorCodes)
	{
		this.urlErrorCodes=urlErrorCodes;
	}
}
