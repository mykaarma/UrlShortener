package com.mykaarma.urlshortener.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mykaarma.urlshortener.exception.ShortUrlException;
import com.mykaarma.urlshortener.persistence.AvailableHashPoolAdapter;
import com.mykaarma.urlshortener.persistence.HashArchiveAdapter;
import com.mykaarma.urlshortener.util.UrlServiceUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@EnableAsync
public class HashGenerationJob {

	@Value("${hash_count_threshold:100000}")
	private int hashCountThreshold;
	
	@Value("${hash_generation_rate:100000}")
	private int hashGenerationRate;
	
	@Value("${hash_length:8}")
	private int hashLength;
	
	private AvailableHashPoolAdapter availableHashPoolAdapter;
	private HashArchiveAdapter hashArchiveAdapter;
	private UrlServiceUtil urlServiceUtil;
	
	@Autowired
	public HashGenerationJob(AvailableHashPoolAdapter availableHashPoolAdapter, HashArchiveAdapter hashArchiveAdapter, UrlServiceUtil urlServiceUtil) {
		this.availableHashPoolAdapter = availableHashPoolAdapter;
		this.hashArchiveAdapter = hashArchiveAdapter;
		this.urlServiceUtil = urlServiceUtil;
	}
	
	@Scheduled(cron = "${hash_generation_cron}")
	public void runHashesGenerationJob() throws ShortUrlException {
		
		int availableHashCount = availableHashPoolAdapter.countAvailableHashes();
		log.info(String.format("Number of available hashes=%d", availableHashCount));
		if(availableHashCount < hashCountThreshold) {
			generateHashes(hashCountThreshold - availableHashCount);
		}
	}
	
	private void generateHashes(int count) throws ShortUrlException {
		
		log.info("Running job for generating hashes");
		int numberOfHashesGenerated = 0;
		while(numberOfHashesGenerated < count) {			
			long randomId = urlServiceUtil.getRandomId(hashLength);
			String shortUrlHash = urlServiceUtil.convertIdToHash(randomId, hashLength);
			
			if(urlServiceUtil.isHashValid(shortUrlHash) && !hashArchiveAdapter.isHashUsed(shortUrlHash)) {
				
				availableHashPoolAdapter.addHashToPool(shortUrlHash);
				hashArchiveAdapter.addHashToArchive(shortUrlHash);
				numberOfHashesGenerated++;
			}
		}
		log.info("Hash generation job completed");
		
	}

	@Scheduled(cron = "${hash_deletion_cron}")
	public void removeUsedHashesFromPool() throws ShortUrlException {
		
		log.info("Deleting used hashes from available hash pool");
		availableHashPoolAdapter.deleteUsedHashes();
	}
}
