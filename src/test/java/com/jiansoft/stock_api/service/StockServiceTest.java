package com.jiansoft.stock_api.service;

import com.jiansoft.stock_api.dto.common.Meta;
import com.jiansoft.stock_api.dto.stock.RevenueDto;
import com.jiansoft.stock_api.provider.StocksDataProvider;
import com.jiansoft.stock_api.support.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StocksDataProvider stocksDataProvider;

    @Test
    void getDividendShouldNormalizeStockSymbolBeforeQuery() {
        when(stocksDataProvider.getDividend("2330")).thenReturn(List.of());
        StockService stockService = new StockService(stocksDataProvider);

        var response = stockService.getDividend(" 2330 ");

        assertEquals(200, response.code());
        verify(stocksDataProvider).getDividend("2330");
    }

    @Test
    void getRevenueByStockShouldNormalizeStockSymbolBeforeQuery() {
        when(
            stocksDataProvider.getRevenueByStock(eq("TSE01"), ArgumentMatchers.any())
        ).thenReturn(emptyRevenuePage());
        StockService stockService = new StockService(stocksDataProvider);

        stockService.getRevenueByStock(" tse01 ", 1, 30);

        verify(stocksDataProvider).getRevenueByStock(eq("TSE01"), ArgumentMatchers.any());
    }

    @Test
    void getDividendShouldRejectBlankStockSymbol() {
        StockService stockService = new StockService(stocksDataProvider);

        assertThrows(IllegalArgumentException.class, () -> stockService.getDividend("   "));
    }

    @Test
    void getRevenueByMonthShouldRejectInvalidMonth() {
        StockService stockService = new StockService(stocksDataProvider);

        assertThrows(
            IllegalArgumentException.class,
            () -> stockService.getRevenueByMonth(202413, 1, 30)
        );
    }

    @Test
    void getHolidayScheduleShouldRejectOutOfRangeYear() {
        StockService stockService = new StockService(stocksDataProvider);

        assertThrows(
            IllegalArgumentException.class,
            () -> stockService.getHolidaySchedule(2200)
        );
    }

    @Test
    void getHistoricalDailyQuoteShouldRejectFutureDate() {
        StockService stockService = new StockService(stocksDataProvider);

        assertThrows(
            IllegalArgumentException.class,
            () -> stockService.getHistoricalDailyQuote(LocalDate.now().plusDays(1), 1, 30)
        );
    }

    private PageResult<RevenueDto> emptyRevenuePage() {
        return new PageResult<>(new Meta(1, 30, 0, 0), List.of());
    }
}
