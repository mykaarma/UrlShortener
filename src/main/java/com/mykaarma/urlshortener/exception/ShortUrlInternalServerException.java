package com.mykaarma.urlshortener.exception;

import com.mykaarma.urlshortener.dto.UrlErrorDTO;
import com.mykaarma.urlshortener.enums.UrlErrorCodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShortUrlInternalServerException extends ShortUrlException{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The error DTO. */
	private UrlErrorDTO errorDTO;
	
	/** The error data. */
	private String errorData;

	/**
	 * Instantiates a new ShortUrlInternalServerException
	 */
	public ShortUrlInternalServerException() {
	}
	
	/**
	 * Instantiates a new ShortUrlInternalServerException
	 *
	 * @param errorEnum the error enum
	 * @param errorDesc the error desc
	 */
	public ShortUrlInternalServerException(UrlErrorCodes errorEnum, String errorDesc) {
		this.errorDTO = new UrlErrorDTO(errorEnum);
		this.errorData = errorDesc;
	}
	
	/**
	 * Instantiates a new ShortUrlInternalServerException
	 *
	 * @param errorDTO the error DTO
	 * @param errorDesc the error desc
	 */
	public ShortUrlInternalServerException(UrlErrorDTO errorDTO, String errorDesc) {
		this.errorDTO = errorDTO;
		this.errorData = errorDesc;
	}
}
