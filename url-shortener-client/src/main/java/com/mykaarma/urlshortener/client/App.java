package com.mykaarma.urlshortener.client;


import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;




import org.springframework.boot.SpringApplication;  

@SpringBootApplication  
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
public class App   
{  
public static void main(String[] args)   
{  
	SpringApplication.run(App.class, args); 

}
}  