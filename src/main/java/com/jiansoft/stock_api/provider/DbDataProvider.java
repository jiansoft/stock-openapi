package com.jiansoft.stock_api.provider;

import com.jiansoft.stock_api.support.CacheDataProvider;

/**
 * 資料庫資料提供者基底類別。
 *
 * <p>集中管理資料庫型資料提供者共用的快取能力。
 */
public abstract class DbDataProvider implements DataProvider {

    protected final CacheDataProvider cacheDataProvider;

    protected DbDataProvider(CacheDataProvider cacheDataProvider) {
        this.cacheDataProvider = cacheDataProvider;
    }
}
