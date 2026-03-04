package com.jiansoft.stock_api.dto.stock;

import java.math.BigDecimal;

/**
 * 營收資料。
 *
 * @param stockSymbol 股票代號
 * @param date 營收年月，格式通常為 {@code yyyyMM}
 * @param monthly 單月營收
 * @param lastMonth 上月營收
 * @param lastYearThisMonth 去年同月營收
 * @param monthlyAccumulated 當年累計營收
 * @param lastYearMonthlyAccumulated 去年同期累計營收
 * @param comparedWithLastMonth 月增率
 * @param comparedWithLastYearSameMonth 年增率
 * @param accumulatedComparedWithLastYear 累計年增率
 * @param avgPrice 當月平均股價
 * @param lowestPrice 當月最低股價
 * @param highestPrice 當月最高股價
 */
public record RevenueDto(
    String stockSymbol,
    long date,
    BigDecimal monthly,
    BigDecimal lastMonth,
    BigDecimal lastYearThisMonth,
    BigDecimal monthlyAccumulated,
    BigDecimal lastYearMonthlyAccumulated,
    BigDecimal comparedWithLastMonth,
    BigDecimal comparedWithLastYearSameMonth,
    BigDecimal accumulatedComparedWithLastYear,
    BigDecimal avgPrice,
    BigDecimal lowestPrice,
    BigDecimal highestPrice
) {
}
