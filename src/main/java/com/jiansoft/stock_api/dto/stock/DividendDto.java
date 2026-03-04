package com.jiansoft.stock_api.dto.stock;

import java.math.BigDecimal;

/**
 * 股利資料。
 *
 * @param exDividendDate1 除息日
 * @param exDividendDate2 除權日
 * @param payableDate1 現金股利發放日
 * @param payableDate2 股票股利發放日
 * @param stockSymbol 股票代號
 * @param quarter 股利所屬季度
 * @param year 股利發放年度
 * @param yearOfDividend 股利所屬年度
 * @param sum 股利總和
 * @param stockDividend 股票股利
 * @param cashDividend 現金股利
 * @param payoutRatio 盈餘分配率
 * @param cashDividendYield 現金殖利率
 * @param earningsPerShare 每股盈餘
 */
public record DividendDto(
    String exDividendDate1,
    String exDividendDate2,
    String payableDate1,
    String payableDate2,
    String stockSymbol,
    String quarter,
    long year,
    long yearOfDividend,
    BigDecimal sum,
    BigDecimal stockDividend,
    BigDecimal cashDividend,
    BigDecimal payoutRatio,
    BigDecimal cashDividendYield,
    BigDecimal earningsPerShare
) {
}
