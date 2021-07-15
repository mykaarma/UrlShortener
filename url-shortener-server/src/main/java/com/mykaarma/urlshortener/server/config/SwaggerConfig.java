package com.mykaarma.urlshortener.server.config;

import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;

import springfox.documentation.service.ApiInfo;

import springfox.documentation.service.Contact;

import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.ModelRendering;
import springfox.documentation.swagger.web.OperationsSorter;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.TagsSorter;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;



import io.micrometer.core.instrument.MeterRegistry;



@Configuration
@EnableSwagger2
public class SwaggerConfig {

	
	
	@Bean
	MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
	return registry -> registry.config().commonTags("application", "adp-customer-adapter","instance",
	"adp-customer-adapter");}

	

	

    /**
     * Swagger Group Configuration for V2
     *
     * @return Docket
     */
    @Bean
    public Docket manageApiV2() {
    	return new Docket(DocumentationType.SWAGGER_2)  
    	          .select()                                  
    	          .apis(RequestHandlerSelectors.basePackage("com.mykaarma.urlshortener.server.controller"))              
    	          .paths(PathSelectors.any())                          
    	          .build()
    	          .apiInfo(apiInfo()); 
    }

    /**
     * Api Description and Info shown in docker page
     *
     * @return ApiInfo
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("url shortener API ").description("The URL SHORTENER " +
                "API is a RESTful API which provides  URL shortening,redirecting and Tracking Methods.")
                .contact(new Contact("myKaarma Support", "https://mykaarma.com",
                        "api-support@mykaarma.com")).version("1.0").build();
    }

    /**
     * Swagger UI Configurations
     *
     * @return UiConfiguration
     */
    @Bean
    public UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder().deepLinking(true).displayOperationId(false).defaultModelsExpandDepth(1).defaultModelExpandDepth(1).defaultModelRendering(ModelRendering.EXAMPLE).displayRequestDuration(false).docExpansion(DocExpansion.NONE).filter(false).maxDisplayedTags(null).operationsSorter(OperationsSorter.ALPHA).showExtensions(false).tagsSorter(TagsSorter.ALPHA)
                //               .supportedSubmitMethods(UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS)
                .validatorUrl(null).build();
    }


    /**
     * Swagger Security Configuration, allows the authorization to be sent in Header from Swagger page.
     *
     * @return SecurityConfiguration
     */
    @Bean
    @SuppressWarnings("deprecation")
    public SecurityConfiguration security() {

        return new SecurityConfiguration(null, null, null, null, "Authorization", ApiKeyVehicle.HEADER, "Authorization", ",");
    }

 
}
