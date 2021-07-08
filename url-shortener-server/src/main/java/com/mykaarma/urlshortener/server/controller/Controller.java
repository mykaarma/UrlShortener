package com.mykaarma.urlshortener.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpServerErrorException.InternalServerError;

import org.springframework.http.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import com.mykaarma.urlshortener.model.ApiError;
import com.mykaarma.urlshortener.model.dto.request.ShortenUrlRequestDTO;
import com.mykaarma.urlshortener.model.dto.response.GetShortUrlClickCountResponseDTO;
import com.mykaarma.urlshortener.model.dto.response.ShortenUrlResponseDTO;
import com.mykaarma.urlshortener.model.enums.UrlErrorCodes;
import com.mykaarma.urlshortener.model.utils.RestURIConstants;
import com.mykaarma.urlshortener.server.service.UrlService;

@RestController
@Slf4j
@Api(tags = "Url Shortener")
public class Controller {

	private UrlService urlService;

	@Autowired
	public Controller(UrlService urlService) {
		super();
		this.urlService = urlService;
	}

	@ApiOperation(value = "shorten Long Url and Return short Url", authorizations = {
			@Authorization(value = "basicAuth") })
	@PostMapping(value = "/" + RestURIConstants.SHORTEN)
	public ResponseEntity<ShortenUrlResponseDTO> shortenUrl(@RequestBody ShortenUrlRequestDTO getShortUrlRequestDTO)
			throws Exception {

		ShortenUrlResponseDTO shortenUrlResponseDTO;
		try {
			shortenUrlResponseDTO = urlService.shortenUrl(getShortUrlRequestDTO);
		} catch (InternalServerError e) {

			shortenUrlResponseDTO = new ShortenUrlResponseDTO();
			List<ApiError> apiErrors = new ArrayList<>();
			ApiError apiError = new ApiError(UrlErrorCodes.INTERNAL_SERVER_ERROR);
			apiErrors.add(apiError);

			shortenUrlResponseDTO.setErrors(apiErrors);
			return new ResponseEntity<>(shortenUrlResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return new ResponseEntity<>(shortenUrlResponseDTO, HttpStatus.OK);

	}

	@ApiOperation(value = "Redirect to Long Url using ShortUrl", authorizations = {
			@Authorization(value = "basicAuth") })
	@GetMapping(value = "/" + RestURIConstants.REDIRECT  + "/"
			+ RestURIConstants.SHORT_URL_HASH_PATH_VARIABLE, produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> redirectToLongUrl(@PathVariable(value = "shortUrlHash") String shortUrlHash) throws Exception {

		String longUrl = null;

		longUrl = urlService.findLongUrl( shortUrlHash);

		String htmlResponse = urlService.getHtmlForRedirectingToLongUrl(longUrl);

		try {
			return new ResponseEntity<>(htmlResponse, HttpStatus.PERMANENT_REDIRECT);
		} catch (Exception e) {
			log.info(e.getMessage());

			String redirectFailureResponse = "<a href=\" " + longUrl + "\""
					+ " target=\"_blank\">Click Here To redirect to The Link!</a>";
			return new ResponseEntity<>(redirectFailureResponse, HttpStatus.OK);
		}
	}

	
}
