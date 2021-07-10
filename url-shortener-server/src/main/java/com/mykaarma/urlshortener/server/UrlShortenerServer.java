package com.mykaarma.urlshortener.server;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.boot.SpringApplication;

@SpringBootApplication
@EnableRedisRepositories
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class})
public class UrlShortenerServer {
	public static void main(String[] args)  {
		
		SpringApplication.run(UrlShortenerServer.class, args);
	}
}