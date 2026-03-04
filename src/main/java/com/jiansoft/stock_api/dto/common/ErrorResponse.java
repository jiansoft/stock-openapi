package com.jiansoft.stock_api.dto.common;

/**
 * API 錯誤回應格式。
 *
 * @param code HTTP 狀態碼
 * @param message 錯誤訊息
 */
public record ErrorResponse(int code, String message) {
}
