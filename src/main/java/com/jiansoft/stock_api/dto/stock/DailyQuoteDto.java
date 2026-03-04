package com.jiansoft.stock_api.dto.stock;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 每日股價資料。
 *
 * @param stockExchangeMarketId 交易市場編號
 * @param stockSymbol 股票代號
 * @param tradingVolume 成交股數
 * @param transaction 成交筆數
 * @param tradeValue 成交金額
 * @param openingPrice 開盤價
 * @param highestPrice 最高價
 * @param lowestPrice 最低價
 * @param closingPrice 收盤價
 * @param changeRange 漲跌幅
 * @param change 漲跌價差
 * @param movingAverage5 五日均線
 * @param movingAverage10 十日均線
 * @param movingAverage20 二十日均線
 * @param movingAverage60 六十日均線
 * @param movingAverage120 一百二十日均線
 * @param movingAverage240 二百四十日均線
 * @param maximumPriceInYear 年內最高價
 * @param minimumPriceInYear 年內最低價
 * @param averagePriceInYear 年內平均價
 * @param maximumPriceInYearDateOn 年內最高價日期
 * @param minimumPriceInYearDateOn 年內最低價日期
 * @param priceToBookRatio 股價淨值比
 */
public record DailyQuoteDto(
    int stockExchangeMarketId,
    String stockSymbol,
    BigDecimal tradingVolume,
    int transaction,
    BigDecimal tradeValue,
    BigDecimal openingPrice,
    BigDecimal highestPrice,
    BigDecimal lowestPrice,
    BigDecimal closingPrice,
    BigDecimal changeRange,
    BigDecimal change,
    BigDecimal movingAverage5,
    BigDecimal movingAverage10,
    BigDecimal movingAverage20,
    BigDecimal movingAverage60,
    BigDecimal movingAverage120,
    BigDecimal movingAverage240,
    BigDecimal maximumPriceInYear,
    BigDecimal minimumPriceInYear,
    BigDecimal averagePriceInYear,
    LocalDate maximumPriceInYearDateOn,
    LocalDate minimumPriceInYearDateOn,
    BigDecimal priceToBookRatio
) {
}
