package com.mykaarma.urlshortener.scheduled;

import java.util.concurrent.locks.Lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mykaarma.urlshortener.enums.RegistryKey;
import com.mykaarma.urlshortener.exception.ShortUrlException;
import com.mykaarma.urlshortener.persistence.AvailableHashPoolAdapter;
import com.mykaarma.urlshortener.persistence.HashArchiveAdapter;
import com.mykaarma.urlshortener.service.RedisLockService;
import com.mykaarma.urlshortener.util.UrlServiceUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
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
	private RedisLockService redisLockService;
	
	@Autowired
	public HashGenerationJob(AvailableHashPoolAdapter availableHashPoolAdapter, HashArchiveAdapter hashArchiveAdapter, UrlServiceUtil urlServiceUtil,
			RedisLockService redisLockService) {
		this.availableHashPoolAdapter = availableHashPoolAdapter;
		this.hashArchiveAdapter = hashArchiveAdapter;
		this.urlServiceUtil = urlServiceUtil;
		this.redisLockService = redisLockService;
	}
	
	@Scheduled(cron = "${hash_generation_cron}")
	public void runHashesGenerationJob() throws ShortUrlException {
		
		Lock lock = null;
        try {
            lock = redisLockService.tryLockOnEntity("runHashGenerationJob", RegistryKey.HASH_GENERATION_JOB);
            if (lock == null || !lock.tryLock()) {
                log.info(" Failed to acquire lock");
                return;
            }
            int availableHashCount = availableHashPoolAdapter.countAvailableHashes();
            log.info(String.format("Number of available hashes=%d", availableHashCount));
            int count = hashCountThreshold - availableHashCount;
            generateHashes(count);
        } catch (Exception e) {
            log.error("error while running HashGenerationJob", e);
        } finally {
            if(lock != null)
                redisLockService.unlock(lock);
        }
	}
	
	public void generateHashes(int count) throws ShortUrlException {
		
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
		
		Lock lock = null;
        try {
            lock = redisLockService.tryLockOnEntity("runHashDeletionJob", RegistryKey.HASH_DELETION_JOB);
            if (lock == null || !lock.tryLock()) {
                log.info(" Failed to acquire lock");
                return;
            }
            log.info("Deleting used hashes from available hash pool");
    		availableHashPoolAdapter.deleteUsedHashes();
        } catch (Exception e) {
            log.error("error while running HashDeletionJob", e);
        } finally {
            if(lock != null)
                redisLockService.unlock(lock);
        }
	}

	public String getHash(){
		long t1 = System.currentTimeMillis();
		Lock lock = null;
		try {
			long randomId = urlServiceUtil.getRandomId(hashLength);
			String shortUrlHash = urlServiceUtil.convertIdToHash(randomId, hashLength);
			lock = redisLockService.tryLockOnEntity("createShortUrlHash_"+shortUrlHash, RegistryKey.HASH_CREATION);
			if (lock == null || !lock.tryLock()) {
				log.warn(" Failed to acquire lock");
			}
			while(!urlServiceUtil.isHashValid(shortUrlHash) || hashArchiveAdapter.isHashUsed(shortUrlHash)) {
				randomId = urlServiceUtil.getRandomId(hashLength);
				shortUrlHash = urlServiceUtil.convertIdToHash(randomId, hashLength);
			}
			hashArchiveAdapter.addHashToArchive(shortUrlHash);
			return shortUrlHash;
		} catch (Exception e){
			log.error("error while generating hash", e);
			return null;
		} finally {
			if(lock != null) {
				redisLockService.unlock(lock);
			}
			log.info("Time taken to getHash for shortUrl is {}ms",System.currentTimeMillis()-t1);
		}
	}
}
