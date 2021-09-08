package org.springframework.boot.autoconfigure.klock.model;

/**
 * Created by kl on 2017/12/29.
 * Content :锁类型
 */
public enum LockType {

    /**
     * 可重入锁，带看门狗，此模式锁释放无效
     */
    ReentrantWatchDog,
    /**
     * 可重入锁
     */
    Reentrant,
    /**
     * 公平锁
     */
    Fair,
    /**
     * 读锁
     */
    Read,
    /**
     * 写锁
     */
    Write;

    LockType() {
    }

}
