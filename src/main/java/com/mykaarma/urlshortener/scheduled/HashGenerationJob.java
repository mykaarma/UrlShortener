package com.mykaarma.urlshortener.scheduled;

import java.util.concurrent.locks.Lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
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
@EnableAsync
public class HashGenerationJob {

	@Value("${hash_count_threshold:100000}")
	private int hashCountThreshold;
	
	@Value("${hash_generation_rate:100000}")
	private int hashGenerationRate;
	
	@Value("${hash_length:8}")
	private int hashLength;

	@Value("${hash_generation_ondemand_count:1000}")
	private int onDemandHashCount;
	
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
		log.info("Hash generation job started");
		Lock lock = null;
        try {
            lock = redisLockService.tryLockOnEntity("runHashGenerationJob", RegistryKey.HASH_GENERATION_JOB);
            if (lock == null || !lock.tryLock()) {
                log.info(" Failed to acquire lock");
                return;
            }
            int availableHashCount = availableHashPoolAdapter.countAvailableHashes();
            log.info("[Before] Number of available hashes={}", availableHashCount);
            int count = hashCountThreshold - availableHashCount;
            generateHashes(count);
			availableHashCount = availableHashPoolAdapter.countAvailableHashes();
			log.info("[After] Number of available hashes={}", availableHashCount);
        } catch (Exception e) {
            log.error("error while running HashGenerationJob", e);
        } finally {
            if(lock != null)
                redisLockService.unlock(lock);
        }
		log.info("Hash generation job completed");
	}

	public void generateHashes(int count) throws ShortUrlException {
		
		log.info("Generating hashes in bulk count={}", count);
		int numberOfHashesGenerated = 0;
		while(numberOfHashesGenerated < count) {
			try {

				long randomId = urlServiceUtil.getRandomId(hashLength);
				String shortUrlHash = urlServiceUtil.convertIdToHash(randomId, hashLength);

				if(urlServiceUtil.isHashValid(shortUrlHash) && !hashArchiveAdapter.isHashUsed(shortUrlHash)) {

					availableHashPoolAdapter.addHashToPool(shortUrlHash);
					hashArchiveAdapter.addHashToArchive(shortUrlHash);
					numberOfHashesGenerated++;
				}

			} catch (Exception e){
				log.error("Error while generating/saving hash countToBeGenerated={} numberOfHashesGenerated={}",count,numberOfHashesGenerated, e);
				count--;
			}
		}
		log.info("Generated hashes in bulk count={}", count);
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
		String shortUrlHash = null;
		try {
			long randomId = urlServiceUtil.getRandomId(hashLength);
			shortUrlHash = urlServiceUtil.convertIdToHash(randomId, hashLength);
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
			log.info("Time taken to getHash {} for shortUrl is {}ms",shortUrlHash, System.currentTimeMillis()-t1);
		}
	}

	@Async
	public void generateHashesAsync(){
		Lock lock = null;
		try {
			lock = redisLockService.tryLockOnEntity("runHashGeneration", RegistryKey.HASH_GENERATION_ONDEMAND);
			if (lock == null || !lock.tryLock()) {
				log.info(" Failed to acquire lock");
				return;
			}
			generateHashes(onDemandHashCount);
		} catch (Exception e){
			log.error("error while running HashGeneration OnDemand", e);
		} finally {
			if(lock != null) {
				redisLockService.unlock(lock);
			}
		}
	}
}
