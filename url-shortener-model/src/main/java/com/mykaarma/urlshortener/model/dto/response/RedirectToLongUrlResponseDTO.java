package com.mykaarma.urlshortener.model.dto.response;

import java.io.Serializable;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class RedirectToLongUrlResponseDTO extends Response implements Serializable  {
	
	private String longUrl;
	

}
