package com.jiansoft.stock_api.support;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CacheDataProviderTest {

    @Test
    void getOrSetShouldRejectNonPositiveTtl() {
        CacheDataProvider cacheDataProvider = new CacheDataProvider();

        assertThrows(
            IllegalArgumentException.class,
            () -> cacheDataProvider.getOrSet("k", Duration.ZERO, () -> "v")
        );
        assertThrows(
            IllegalArgumentException.class,
            () -> cacheDataProvider.getOrSet("k", Duration.ofSeconds(-1), () -> "v")
        );
    }

    @Test
    void getOrSetShouldCacheNullValue() {
        CacheDataProvider cacheDataProvider = new CacheDataProvider();
        AtomicInteger counter = new AtomicInteger();

        String first = cacheDataProvider.getOrSet("k", Duration.ofSeconds(1), () -> {
            counter.incrementAndGet();
            return null;
        });
        String second = cacheDataProvider.getOrSet("k", Duration.ofSeconds(1), () -> {
            counter.incrementAndGet();
            return "new-value";
        });

        assertNull(first);
        assertNull(second);
        assertEquals(1, counter.get());
    }

    @Test
    void getOrSetShouldRecomputeAfterExpiration() throws InterruptedException {
        CacheDataProvider cacheDataProvider = new CacheDataProvider();
        AtomicInteger counter = new AtomicInteger();

        int first = cacheDataProvider.getOrSet(
            "k",
            Duration.ofMillis(30),
            counter::incrementAndGet
        );

        Thread.sleep(80);

        int second = cacheDataProvider.getOrSet(
            "k",
            Duration.ofMillis(30),
            counter::incrementAndGet
        );

        assertEquals(1, first);
        assertEquals(2, second);
    }
}
