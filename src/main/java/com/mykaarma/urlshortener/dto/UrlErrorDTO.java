package com.mykaarma.urlshortener.dto;

import java.io.Serializable;

import com.mykaarma.urlshortener.enums.UrlErrorCodes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UrlErrorDTO implements Serializable {

	/** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The error code. */
    private int errorCode;
    
    /** The error title. */
    private String errorTitle;
    
    /** The error message. */
    private String errorMessage;

    /**
     * Instantiates a new url error DTO.
     *
     * @param errorCodes the error codes
     */
    public UrlErrorDTO(UrlErrorCodes errorCodes) {
        super();
        this.errorCode = errorCodes.getErrorCode();
        this.errorTitle = errorCodes.getErrorTitle();
        this.errorMessage = errorCodes.getErrorMessage();
    }
}
