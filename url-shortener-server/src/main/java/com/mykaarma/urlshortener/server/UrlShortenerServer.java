package com.mykaarma.urlshortener.server;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;



import org.springframework.boot.SpringApplication;

@SpringBootApplication
@EnableCaching
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
public class UrlShortenerServer {
	public static void main(String[] args)  {
		
		SpringApplication.run(UrlShortenerServer.class, args);
	}
}