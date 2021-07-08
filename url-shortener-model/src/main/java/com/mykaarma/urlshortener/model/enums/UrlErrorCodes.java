package com.mykaarma.urlshortener.model.enums;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum UrlErrorCodes {
	
	//CREDENTIALS_ARE_NULL(100001, "NULL_CREDENTIALS", "When login credentials are missing from header", "Auth credentials are empty or null"),
	INTERNAL_SERVER_ERROR(100010, "INTERNAL_SERVER_ERROR", "Internal Server Error", "Internal Server Error"),
	INVALID_LONG_URL(100141,"INVALID_LONG_URL","The entered Url is Not Valid","Invalid Long Url is Entered "),
	INVALID_SHORT_URL(100142,"INVALID_ShORT_URL","The entered Short Url is Not Valid","Invalid Short Url is Entered for  Redirection"),
	SHORT_URL_NOT_FOUND(100143,"SHORT_URL_NOT_FOUND","The Short Url Entered is not Found","No Such Short Url Present in Database "),
	BAD_REQUEST(100144,"BAD_REQUEST","Parameters Entered are not Correct ","Parameters Entered are not Correct "),
	INVALID_DURATION_FORMAT(100145,"INVALID_DURATION_FORMAT","Duration Format is InValid ","Duration Format is InValid ");
	

	private Integer errorCode;
	private String errorTitle;
	private String errorDescription;
	private String errorMessage;
	
	

	
	
	

}
