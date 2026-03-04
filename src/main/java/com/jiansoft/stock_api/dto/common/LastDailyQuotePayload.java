package com.jiansoft.stock_api.dto.common;

import java.util.List;

/**
 * 最後一個交易日報價的資料負載。
 *
 * @param <T> 資料項目型別
 * @param date 最後收盤日期
 * @param meta 分頁資訊
 * @param data 當前頁面的資料清單
 */
public record LastDailyQuotePayload<T>(String date, Meta meta, List<T> data) {
}
