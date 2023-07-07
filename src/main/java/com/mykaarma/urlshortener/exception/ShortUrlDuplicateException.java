package com.mykaarma.urlshortener.exception;

import com.mykaarma.urlshortener.dto.UrlErrorDTO;
import com.mykaarma.urlshortener.enums.UrlErrorCodes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShortUrlDuplicateException extends ShortUrlException{
    private UrlErrorDTO errorDTO;


    private String errorData;

    public ShortUrlDuplicateException(UrlErrorCodes errorEnum, String errorDesc) {
        this.errorDTO = new UrlErrorDTO(errorEnum);
        this.errorData = errorDesc;
    }

    public ShortUrlDuplicateException(UrlErrorDTO errorDTO, String errorDesc){
        this.errorDTO = errorDTO;
        this.errorData = errorDesc;
    }


}
