package com.mykaarma.urlshortener.exception;

import com.mykaarma.urlshortener.enums.UrlErrorCodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ShortUrlNotFoundException extends ShortUrlException {
	
	private UrlErrorCodes urlErrorCodes;
}
