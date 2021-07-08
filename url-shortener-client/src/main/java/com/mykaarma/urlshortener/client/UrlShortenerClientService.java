package com.mykaarma.urlshortener.client;



import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;

import org.springframework.web.client.HttpServerErrorException.InternalServerError;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mykaarma.urlshortener.client.service.UrlShorteningService;
import com.mykaarma.urlshortener.model.ApiError;
import com.mykaarma.urlshortener.model.dto.request.ShortenUrlRequestDTO;
import com.mykaarma.urlshortener.model.dto.response.GetShortUrlClickCountResponseDTO;
import com.mykaarma.urlshortener.model.dto.response.ShortenUrlResponseDTO;
import com.mykaarma.urlshortener.model.enums.UrlErrorCodes;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.*;


public class UrlShortenerClientService {
	
	private UrlShorteningService urlShorteningService;
	Logger logger = LoggerFactory.getLogger(UrlShortenerClientService.class);
	
	public UrlShortenerClientService(String baseUrl)
	{
		createRetrofit(baseUrl);
	}
	
	private void createRetrofit(String baseUrl) {
		OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
		addLoggingHeader(httpClient);

		httpClient.connectTimeout(1, TimeUnit.MINUTES).readTimeout(30, TimeUnit.SECONDS).
			writeTimeout(15, TimeUnit.SECONDS);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Retrofit jsonRetrofit = new Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).client(httpClient.build()).build();
		urlShorteningService = jsonRetrofit.create(UrlShorteningService.class);
		
	}
	private void addLoggingHeader(OkHttpClient.Builder httpClient) {
		
		HttpLoggingInterceptor logging = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC);
		if (!httpClient.interceptors().contains(logging)) {
			httpClient.addInterceptor(logging);
		}
	}
	
	
	
	
	
	

	 public ShortenUrlResponseDTO  shortenUrl( ShortenUrlRequestDTO getShortUrlRequestDTO) 
	 {
		
		 Call<ShortenUrlResponseDTO> callResponse=urlShorteningService.shortenUrl(getShortUrlRequestDTO);
		 logger.info("done1");
		 try {
		Response<ShortenUrlResponseDTO>response=callResponse.execute();
		
		
	     return response.body();
	     }
		 catch (InternalServerError ex) {
			 
			 logger.info("encountered exception while executing response :{}",ex.getMessage());
			 ShortenUrlResponseDTO shortenUrlResponseDTO=new ShortenUrlResponseDTO();
			 
			 ApiError apiError=new ApiError(UrlErrorCodes.INTERNAL_SERVER_ERROR);
			 List<ApiError> errors = new ArrayList<>();
			errors.add(apiError);
			shortenUrlResponseDTO.setErrors(errors);
			return shortenUrlResponseDTO;
			 
		
	        }
		 catch(Exception ex)
		 {
			 logger.info("encountered exception while executing response due to bad request :{}",ex.getMessage());
			 ShortenUrlResponseDTO shortenUrlResponseDTO=new ShortenUrlResponseDTO();
			 
			 ApiError apiError=new ApiError(UrlErrorCodes.BAD_REQUEST);
			 List<ApiError> errors = new ArrayList<>();
			errors.add(apiError);
			shortenUrlResponseDTO.setErrors(errors);
			return shortenUrlResponseDTO;
			 
			 
		 }
		 
		
		 
		 
	       
	 }
	
	
	public String redirectToLongUrl(String scheme, String shortUrlDomain, String shortUrlHash)throws ResponseStatusException
	{
		Call<String> callResponse=urlShorteningService.redirectToLongUrl(scheme,shortUrlDomain,shortUrlHash);
		
		 try {
			 
			Response <String> response = callResponse.execute();
			
		     return response.body(); 
			 }
		 catch (Exception ex) 
		 {
			 
			  throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			 
		 }
	}
	
	
	
	
	
	
}
