package com.mykaarma.urlshortener.model;

import java.io.Serializable;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.mykaarma.urlshortener.model.enums.UrlErrorCodes;

public class ApiError implements Serializable {
	public ApiError() {
		
	}
	
	public ApiError(UrlErrorCodes urlErrorCodes) {
		super();
		this.errorCode = urlErrorCodes.getErrorCode();
		this.errorTitle = urlErrorCodes.getErrorTitle();
		this.errorMessage=urlErrorCodes.getErrorMessage();
		
		
	}
	
	
	


	private Integer errorCode;
	
	private String errorTitle;
	
	private String errorMessage;

	@JsonProperty("errorCode")
	public Integer getErrorCode() {
		return errorCode;
	}
	@JsonProperty("errorTitle")
	public String geterrorTitle() {
		return errorTitle;
	}

	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

	public void seterrorTitle(String errorTitle) {
		this.errorTitle = errorTitle;
	}

	@JsonProperty("errorMessage")
	public String geterrorMessage() {
		return errorMessage;
	}

	public void seterrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
