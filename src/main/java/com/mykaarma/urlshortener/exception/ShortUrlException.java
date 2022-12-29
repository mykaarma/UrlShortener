package com.mykaarma.urlshortener.exception;


import com.mykaarma.urlshortener.enums.UrlErrorCodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlException extends RuntimeException{
	
	private UrlErrorCodes errorCode;

}
