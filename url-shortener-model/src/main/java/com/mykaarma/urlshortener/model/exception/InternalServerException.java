package com.mykaarma.urlshortener.model.exception;

import com.mykaarma.urlshortener.model.enums.UrlErrorCodes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class InternalServerException  extends Exception{
	private UrlErrorCodes urlErrorCodes;
}
