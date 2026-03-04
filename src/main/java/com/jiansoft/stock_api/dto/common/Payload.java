package com.jiansoft.stock_api.dto.common;

/**
 * 單純資料負載結構。
 *
 * @param <T> 資料型別
 * @param data 實際資料內容
 */
public record Payload<T>(T data) {
}
