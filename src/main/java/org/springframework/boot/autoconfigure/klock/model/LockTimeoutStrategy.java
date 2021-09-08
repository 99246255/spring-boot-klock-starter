package org.springframework.boot.autoconfigure.klock.model;

import org.aspectj.lang.JoinPoint;
import org.springframework.boot.autoconfigure.klock.handler.KlockTimeoutException;
import org.springframework.boot.autoconfigure.klock.handler.lock.LockTimeoutHandler;
import org.springframework.boot.autoconfigure.klock.lock.Lock;

import java.util.concurrent.TimeUnit;


/**
 * @author wanglaomo
 * @since 2019/4/15
 **/
public enum LockTimeoutStrategy implements LockTimeoutHandler {

    /**
     * 继续执行业务逻辑，不做任何处理
     */
    NO_OPERATION() {
        @Override
        public void handle(LockInfo lockInfo, Lock lock, JoinPoint joinPoint) {
            // do nothing
        }
    },

    /**
     * 快速失败
     */
    FAIL_FAST() {
        @Override
        public void handle(LockInfo lockInfo, Lock lock, JoinPoint joinPoint) {
            String errorMsg = String.format("获取锁(%s)超时(%ds)", lockInfo.getName(), lockInfo.getWaitTime());
            throw new KlockTimeoutException(errorMsg);
        }
    },

    /**
     * 一直阻塞，直到获得锁，100次，500秒左右在太多的尝试后，仍会报错
     */
    KEEP_ACQUIRE() {

        private static final long DEFAULT_INTERVAL = 100L;

        /**
         * 重试100次，从100毫秒，每次增加100毫秒，100次后是500秒左右
         */
        private static final int MAX_RETRY_COUNT = 100;

        @Override
        public void handle(LockInfo lockInfo, Lock lock, JoinPoint joinPoint) {

            long interval = DEFAULT_INTERVAL;

            int count = 0;
            while(!lock.acquire()) {
                if(count > 100) {
                    String errorMsg = String.format("获取锁(%s)重复次数过多，可能死锁", lockInfo.getName());
                    throw new KlockTimeoutException(errorMsg);
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(interval);
                    interval += DEFAULT_INTERVAL;
                    count++;
                } catch (InterruptedException e) {
                    throw new KlockTimeoutException(String.format("获取锁(%s)被中断失败", lockInfo.getName()), e);
                }
            }
        }
    }


}