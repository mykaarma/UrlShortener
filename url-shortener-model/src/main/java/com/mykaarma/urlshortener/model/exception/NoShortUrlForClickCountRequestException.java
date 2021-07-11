package com.mykaarma.urlshortener.model.exception;

import com.mykaarma.urlshortener.model.enums.UrlErrorCodes;


import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class NoShortUrlForClickCountRequestException extends Exception{
	private UrlErrorCodes urlErrorCodes;
	
	public NoShortUrlForClickCountRequestException(UrlErrorCodes urlErrorCodes)
	{
		this.urlErrorCodes=urlErrorCodes;
	}
}
