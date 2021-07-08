package com.mykaarma.urlshortener.server.advice;


import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;


import com.mykaarma.urlshortener.model.exception.BadRedirectingRequestException;
import com.mykaarma.urlshortener.model.exception.BadShorteningRequestException;
import com.mykaarma.urlshortener.model.exception.NoSuchElementFoundException;
import com.mykaarma.urlshortener.model.ApiError;
import com.mykaarma.urlshortener.model.dto.response.ShortenUrlResponseDTO;
import com.mykaarma.urlshortener.model.enums.UrlErrorCodes;

@ControllerAdvice
public class UrlControllerAdvice {
	
	@ExceptionHandler(BadRedirectingRequestException.class)
	public ResponseEntity<String>handleBadRedirectRequest(BadRedirectingRequestException badRequestException)
	{
		return new ResponseEntity<>("This is A Bad Request. The parameters entered are Invalid",HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NoSuchElementFoundException.class)
	public ResponseEntity<String>handleNoSuchElementFoundException(NoSuchElementFoundException noSuchElementFoundException)
	{
		return new ResponseEntity<>("No Url Exists For this Request ",HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(InternalServerError.class)
	public ResponseEntity<String>handleInternalServerErrorException(InternalServerError internalServerError)
	{
		return new ResponseEntity<>("Internal Server Error . The Request Couldnot be Processed ",HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	
	@ExceptionHandler(BadShorteningRequestException.class)
	public ResponseEntity<ShortenUrlResponseDTO>handleBadShorteningRequest(BadShorteningRequestException badRequestException)
	{
		ShortenUrlResponseDTO shortenUrlResponseDTO =new ShortenUrlResponseDTO();
		
		List<ApiError>apiErrors =new ArrayList<>() ;
		ApiError apiError=new ApiError(UrlErrorCodes.BAD_REQUEST);
		apiErrors.add(apiError);
		shortenUrlResponseDTO.setErrors(apiErrors);
		
		
		
		return new ResponseEntity<>(shortenUrlResponseDTO,HttpStatus.BAD_REQUEST);
	}
	
	
}
