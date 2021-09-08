package org.springframework.boot.autoconfigure.klock.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.klock.model.LockInfo;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author: chenyu
 * @Date: 2021/9/8 11:36
 * @Description:
 */
public class ReentrantWatchDogLock implements Lock {

	private RLock rLock;

	private final LockInfo lockInfo;

	private RedissonClient redissonClient;

	public ReentrantWatchDogLock(RedissonClient redissonClient,LockInfo lockInfo) {
		this.redissonClient = redissonClient;
		this.lockInfo = lockInfo;
	}


	@Override
	public boolean acquire() {
		try {
			rLock = redissonClient.getLock(lockInfo.getName());
			return rLock.tryLock(lockInfo.getWaitTime(), TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}

	@Override
	public boolean release() {
		if(rLock.isHeldByCurrentThread()){
			try {
				return rLock.forceUnlockAsync().get();
			} catch (InterruptedException e) {
				return false;
			} catch (ExecutionException e) {
				return false;
			}
		}
		return false;
	}
}
