package com.mykaarma.urlshortener.client.service;






import com.mykaarma.urlshortener.model.dto.request.ShortenUrlRequestDTO;
import com.mykaarma.urlshortener.model.dto.response.GetShortUrlClickCountResponseDTO;
import com.mykaarma.urlshortener.model.dto.response.ShortenUrlResponseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Path;



public interface UrlShorteningService {
	
	@POST("/shorten")
	Call< ShortenUrlResponseDTO> shortenUrl(@Body ShortenUrlRequestDTO longUrlDto);
	
	@HTTP(method="GET",path = "/redirect/{scheme}://{shortUrlDomain}/{shortUrlHash}")
	 Call<String> redirectToLongUrl( @Path("scheme") String scheme, @Path("shortUrlDomain") String shortUrlDomain,@Path("shortUrlHash")String shortUrlHash);
	
	
	

}
