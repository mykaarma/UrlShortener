package com.mykaarma.urlshortener.server.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;

import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityScheme;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import java.util.Set;
import com.fasterxml.classmate.TypeResolver;

import io.micrometer.core.instrument.MeterRegistry;



@Configuration
@EnableSwagger2
public class SwaggerConfig {
	@Autowired
	private TypeResolver typeResolver;
	
	
	
	

	

    /**
     * Swagger Group Configuration for V2
     *
     * @return Docket
     */
    @Bean
    public Docket manageApiV2() {
    	return new Docket(DocumentationType.SWAGGER_2)  
    	          .select()                                  
    	          .apis(RequestHandlerSelectors.any())              
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
                "API is a RESTful API which provides several URL shortening and redirect Method.")
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

    /**
     * List of security schemes supported i.e BasicAuth for mK
     *
     * @return SecuritySchemes
     */
    private List<SecurityScheme> defaultSecuritySchemes() {
        List<SecurityScheme> schemeList = new ArrayList<>();
        schemeList.add(new BasicAuth("basicAuth"));
        return schemeList;
    }

    private Set<String> produces(){
        Set<String> endPointProduces = new HashSet<>();
        endPointProduces.add("APPLICATION_JSON");
        return endPointProduces;
    }

}
