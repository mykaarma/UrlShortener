package com.mykaarma.urlshortener.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * AvailableHashPool maintains a pool of hashes, which can be used by the url-shortener. isValid true indicates that the hashes are unused;
 * @author Aadi Shukla
 *
 */
@Data
@AllArgsConstructor
public class AvailableHashPool implements Serializable {

	private String shortUrlHash;
	private boolean isAvailable;
}
