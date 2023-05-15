package com.mykaarma.urlshortener.exception;


import com.mykaarma.urlshortener.dto.UrlErrorDTO;
import com.mykaarma.urlshortener.enums.UrlErrorCodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class ShortUrlException extends RuntimeException {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The error DTO. */
	private UrlErrorDTO errorDTO;
	
	/** The error data. */
	private String errorData;

	/**
	 * Instantiates a new ShortUrlException
	 */
	public ShortUrlException() {
	}
	
	/**
	 * Instantiates a new ShortUrlException
	 *
	 * @param errorEnum the error enum
	 * @param errorDesc the error desc
	 */
	public ShortUrlException(UrlErrorCodes errorEnum, String errorDesc) {
		this.errorDTO = new UrlErrorDTO(errorEnum);
		this.errorData = errorDesc;
	}
	
	/**
	 * Instantiates a new ShortUrlException
	 *
	 * @param errorDTO the error DTO
	 * @param errorDesc the error desc
	 */
	public ShortUrlException(UrlErrorDTO errorDTO, String errorDesc) {
		this.errorDTO = errorDTO;
		this.errorData = errorDesc;
	}
}
