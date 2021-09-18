package com.mykaarma.urlshortener.model;



import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiWarning implements Serializable{
	public ApiWarning(){
		
	}
	
	private String warningCode;

	private String warningDescription;

	@JsonProperty("warningCode")
	public String getWarningCode() {
		return warningCode;
	}

	@JsonProperty("warningDescription")
	public String getWarningDescription() {
		return warningDescription;
	}

	public void setWarningCode(String warningCode) {
		this.warningCode = warningCode;
	}

	public void setWarningDescription(String warningDescription) {
		this.warningDescription = warningDescription;
	}

	public ApiWarning(String warningCode, String warningDescription) {
		super();
		this.warningCode = warningCode;
		this.warningDescription = warningDescription;
	}

}
