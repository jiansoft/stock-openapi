package com.jiansoft.stock_api.dto.stock;

import java.math.BigDecimal;

/**
 * 股票基本資料。
 *
 * @param stockSymbol 股票代號
 * @param name 股票名稱
 * @param lastOneEps 最近一季每股盈餘
 * @param lastFourEps 近四季累計每股盈餘
 * @param weight 權值比重
 * @param industryId 產業別編號
 * @param exchangeMarketId 交易市場編號
 */
public record DetailDto(
    String stockSymbol,
    String name,
    BigDecimal lastOneEps,
    BigDecimal lastFourEps,
    BigDecimal weight,
    int industryId,
    int exchangeMarketId
) {
}
