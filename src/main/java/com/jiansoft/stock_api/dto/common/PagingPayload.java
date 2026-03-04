package com.jiansoft.stock_api.dto.common;

import java.util.List;

/**
 * 含分頁資訊的資料負載。
 *
 * @param <T> 資料項目型別
 * @param meta 分頁資訊
 * @param data 當前頁面的資料清單
 */
public record PagingPayload<T>(Meta meta, List<T> data) {
}
