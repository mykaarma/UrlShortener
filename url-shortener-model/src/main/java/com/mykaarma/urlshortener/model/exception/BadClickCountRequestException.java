package com.mykaarma.urlshortener.model.exception;

import com.mykaarma.urlshortener.model.enums.UrlErrorCodes;


import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class BadClickCountRequestException extends Exception{
	private UrlErrorCodes urlErrorCodes;
	
	public BadClickCountRequestException(UrlErrorCodes urlErrorCodes)
	{
		this.urlErrorCodes=urlErrorCodes;
	}
}
