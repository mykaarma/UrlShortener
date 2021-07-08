package com.mykaarma.urlshortener.model.exception;

import com.mykaarma.urlshortener.model.enums.UrlErrorCodes;

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

public class Exception  extends RuntimeException{
	
	private  UrlErrorCodes errorCode;


	
	

}
