package com.jiansoft.stock_api.support;

import com.jiansoft.stock_api.provider.DataProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 應用程式內部記憶體快取提供者。
 *
 * <p>此類別對齊原始 C# 專案的 {@code CacheDataProvider} 概念，提供：
 * 每個 key 的防重算鎖定、依 TTL 過期、以及定時清理已過期的快取與鎖。
 */
@Component
public class CacheDataProvider implements DataProvider {

    private static final Object NULL_VALUE = new Object();

    private final ConcurrentHashMap<String, CacheEntry> cacheEntries = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LockInfo> locks = new ConcurrentHashMap<>();

    /**
     * 讀取快取；若不存在或已過期，則執行 factory 產生資料並寫回快取。
     *
     * @param key 快取鍵值
     * @param ttl 快取有效時間
     * @param factory 用來產生資料的邏輯
     * @param <T> 快取資料型別
     * @return 快取資料，或 factory 新產生的資料
     */
    public <T> T getOrSet(String key, Duration ttl, Supplier<T> factory) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(ttl, "ttl must not be null");
        Objects.requireNonNull(factory, "factory must not be null");
        if (ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("ttl must be greater than zero");
        }

        CachedValue<T> cachedValue = getCachedValue(key);
        if (cachedValue.hit()) {
            return cachedValue.value();
        }

        ReentrantLock lock = getLockForKey(key, ttl);
        lock.lock();

        try {
            CachedValue<T> doubleChecked = getCachedValue(key);
            if (doubleChecked.hit()) {
                return doubleChecked.value();
            }

            T computedValue = factory.get();
            putValue(key, computedValue, ttl);

            return computedValue;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 刪除指定 key 的快取與對應鎖資訊。
     *
     * @param key 快取鍵值
     */
    public void remove(String key) {
        cacheEntries.remove(key);
        locks.remove(key);
    }

    /**
     * 每小時清理一次過期快取與鎖，避免長期累積無用資料。
     */
    @Scheduled(fixedDelay = 3_600_000L)
    void cleanupExpiredEntries() {
        Instant now = Instant.now();

        cacheEntries.entrySet().removeIf(entry -> isExpired(entry.getValue().expiresAt(), now));
        locks.entrySet().removeIf(entry -> isExpired(entry.getValue().expiresAt(), now));
    }

    private ReentrantLock getLockForKey(String key, Duration ttl) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(ttl);
        LockInfo lockInfo = locks.compute(key, (ignored, existing) -> {
            if (existing == null || isExpired(existing.expiresAt(), now)) {
                return new LockInfo(new ReentrantLock(), expiresAt);
            }

            return new LockInfo(existing.lock(), expiresAt);
        });

        return lockInfo.lock();
    }

    @SuppressWarnings("unchecked")
    private <T> CachedValue<T> getCachedValue(String key) {
        CacheEntry cacheEntry = cacheEntries.get(key);
        if (cacheEntry == null) {
            return new CachedValue<>(false, null);
        }

        Instant now = Instant.now();
        if (isExpired(cacheEntry.expiresAt(), now)) {
            cacheEntries.remove(key, cacheEntry);
            return new CachedValue<>(false, null);
        }

        if (cacheEntry.value() == NULL_VALUE) {
            return new CachedValue<>(true, null);
        }

        return new CachedValue<>(true, (T) cacheEntry.value());
    }

    private <T> void putValue(String key, T value, Duration ttl) {
        Object cacheValue = value == null ? NULL_VALUE : value;
        Instant now = Instant.now();
        cacheEntries.put(key, new CacheEntry(cacheValue, now.plus(ttl)));
    }

    private boolean isExpired(Instant expiresAt, Instant now) {
        return !expiresAt.isAfter(now);
    }

    private record CacheEntry(Object value, Instant expiresAt) {
    }

    private record LockInfo(ReentrantLock lock, Instant expiresAt) {
    }

    private record CachedValue<T>(boolean hit, T value) {
    }
}
