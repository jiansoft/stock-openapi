package com.jiansoft.stock_api.dto.stock;

/**
 * 股票產業分類資料。
 *
 * @param industryId 產業別編號
 * @param name 產業名稱
 */
public record IndustryDto(int industryId, String name) {
}
