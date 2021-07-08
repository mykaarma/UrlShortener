package com.mykaarma.urlshortener.model.jpa;



import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ShortUrlAttributes")
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

public class ShortUrlAttributes implements Serializable {
	@Id
	private Long id;
	private Long version;
	private String shortUrl;
	private String eventCategory;
	private String eventLabel;
	private String eventAction;
	private Long eventValue;
	private String additionalParamsJson;
	private Date ttl;

}
