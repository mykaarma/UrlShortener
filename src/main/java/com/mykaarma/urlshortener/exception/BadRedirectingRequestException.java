package com.mykaarma.urlshortener.exception;

import com.mykaarma.urlshortener.enums.UrlErrorCodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class BadRedirectingRequestException extends ShortUrlException {
	
	private UrlErrorCodes urlErrorCodes;
}
