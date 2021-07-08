package com.mykaarma.urlshortener.model.exception;



import com.mykaarma.urlshortener.model.enums.UrlErrorCodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NoSuchElementFoundException extends Exception {
	private UrlErrorCodes urlErrorCodes;
}
