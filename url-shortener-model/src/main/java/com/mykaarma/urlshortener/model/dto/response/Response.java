package com.mykaarma.urlshortener.model.dto.response;

import java.util.List;
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mykaarma.urlshortener.model.ApiError;
import com.mykaarma.urlshortener.model.ApiWarning;

public class Response implements Serializable{
private static final long serialVersionUID = 1L;
	
	private List<ApiError> errors;
	private List<ApiWarning> warnings;
	
	
	@JsonProperty(value="warnings")
	public List<ApiWarning> getWarnings() {
		return warnings;
	}
	public void setWarnings(List<ApiWarning> warnings) {
		this.warnings = warnings;
	}
	
	@JsonProperty(value="errors")
	public List<ApiError> getErrors() {
		return errors;
	}
	public void setErrors(List<ApiError> errors) {
		this.errors = errors;
	}
	

}
