package com.jiansoft.stock_api.dto.common;

/**
 * API 標準回應外層結構。
 *
 * @param <T> 負載資料型別
 * @param code HTTP 狀態碼
 * @param payload 回應負載
 */
public record ApiResponse<T>(int code, T payload) {
}
