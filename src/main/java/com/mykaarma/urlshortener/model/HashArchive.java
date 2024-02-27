package com.mykaarma.urlshortener.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * HashArchive maintains a list of all the archives that have been used to ensure that a hash never gets re-used in the future.
 * @author Aadi Shukla
 *
 */
@Data
@AllArgsConstructor
public class HashArchive implements Serializable {

	private String shortUrlHash;
}