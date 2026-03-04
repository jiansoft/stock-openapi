package com.jiansoft.stock_api.dto.twse;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 台灣加權股價指數資料。
 *
 * @param date 資料日期
 * @param tradingVolume 成交股數
 * @param transaction 成交筆數
 * @param tradeValue 成交金額
 * @param change 漲跌點數
 * @param index 指數值
 */
public record TaiexDto(
    LocalDate date,
    BigDecimal tradingVolume,
    BigDecimal transaction,
    BigDecimal tradeValue,
    BigDecimal change,
    BigDecimal index
) {
}
