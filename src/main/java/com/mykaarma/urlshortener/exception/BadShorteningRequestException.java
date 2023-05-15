package com.mykaarma.urlshortener.exception;

import com.mykaarma.urlshortener.dto.UrlErrorDTO;
import com.mykaarma.urlshortener.enums.UrlErrorCodes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadShorteningRequestException extends ShortUrlException{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The error DTO. */
	private UrlErrorDTO errorDTO;
	
	/** The error data. */
	private String errorData;

	/**
	 * Instantiates a new BadShorteningRequestException
	 */
	public BadShorteningRequestException() {
	}
	
	/**
	 * Instantiates a new BadShorteningRequestException
	 *
	 * @param errorEnum the error enum
	 * @param errorDesc the error desc
	 */
	public BadShorteningRequestException(UrlErrorCodes errorEnum, String errorDesc) {
		this.errorDTO = new UrlErrorDTO(errorEnum);
		this.errorData = errorDesc;
	}
	
	/**
	 * Instantiates a new BadShorteningRequestException
	 *
	 * @param errorDTO the error DTO
	 * @param errorDesc the error desc
	 */
	public BadShorteningRequestException(UrlErrorDTO errorDTO, String errorDesc) {
		this.errorDTO = errorDTO;
		this.errorData = errorDesc;
	}

}
