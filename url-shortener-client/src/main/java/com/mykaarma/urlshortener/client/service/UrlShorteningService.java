package com.mykaarma.urlshortener.client.service;






import com.mykaarma.urlshortener.model.dto.request.ShortenUrlRequestDTO;
import com.mykaarma.urlshortener.model.dto.response.GetShortUrlClickCountResponseDTO;
import com.mykaarma.urlshortener.model.dto.response.ShortenUrlResponseDTO;
import com.mykaarma.urlshortener.model.utils.RestURIConstants;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.Path;



public interface UrlShorteningService {
	
	
	@POST("/" + RestURIConstants.SHORTEN)
	Call< ShortenUrlResponseDTO> shortenUrl(@Body ShortenUrlRequestDTO longUrlDto);
	
	@HTTP(method="GET",path = "/"+  RestURIConstants.REDIRECT +"/"+RestURIConstants.SHORT_URL_HASH_PATH_VARIABLE)
	 Call<String> redirectToLongUrl( @Path("shortUrlHash")String shortUrlHash);
	
	@GET("/"+ RestURIConstants.COUNT_CLICKS +"/"+ RestURIConstants.SHORT_URL_HASH_PATH_VARIABLE   )
	 Call<GetShortUrlClickCountResponseDTO> countClicks( @Path("shortUrlHash")String shortUrlHash);

}
