package com.jiansoft.stock_api.provider;

import com.jiansoft.stock_api.dto.stock.DailyQuoteDto;
import com.jiansoft.stock_api.dto.stock.DetailDto;
import com.jiansoft.stock_api.dto.stock.DividendDto;
import com.jiansoft.stock_api.dto.stock.HolidayScheduleDto;
import com.jiansoft.stock_api.dto.stock.IndustryDto;
import com.jiansoft.stock_api.dto.stock.RevenueDto;
import com.jiansoft.stock_api.repository.StockRepository;
import com.jiansoft.stock_api.service.StockGrpcClient;
import com.jiansoft.stock_api.support.CacheDataProvider;
import com.jiansoft.stock_api.support.PageResult;
import com.jiansoft.stock_api.support.PaginationRequest;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 股票資料提供者。
 *
 * <p>將股票相關的資料查詢與快取集中在同一層，對齊原始 C# 專案的設計概念。
 */
@Component
public class StocksDataProvider extends DbDataProvider {

    private final StockRepository stockRepository;
    private final StockGrpcClient stockGrpcClient;

    public StocksDataProvider(
        CacheDataProvider cacheDataProvider,
        StockRepository stockRepository,
        StockGrpcClient stockGrpcClient
    ) {
        super(cacheDataProvider);
        this.stockRepository = stockRepository;
        this.stockGrpcClient = stockGrpcClient;
    }

    /**
     * 取得股票基本資料。
     *
     * @param paginationRequest 分頁請求
     * @return 股票基本資料分頁結果
     */
    public PageResult<DetailDto> getDetails(PaginationRequest paginationRequest) {
        String cacheKey = "stock:details:%d:%d".formatted(
            paginationRequest.requestedPage(),
            paginationRequest.recordsPerPage()
        );

        return cacheDataProvider.getOrSet(
            cacheKey,
            Duration.ofDays(1),
            () -> stockRepository.findDetails(paginationRequest)
        );
    }

    /**
     * 取得股票產業分類。
     *
     * @return 股票產業分類清單
     */
    public List<IndustryDto> getIndustries() {
        return cacheDataProvider.getOrSet(
            "stock:industries",
            Duration.ofDays(30),
            stockRepository::findIndustries
        );
    }

    /**
     * 取得指定股票的股利資料。
     *
     * @param stockSymbol 股票代號
     * @return 股利資料清單
     */
    public List<DividendDto> getDividend(String stockSymbol) {
        String cacheKey = "stock:dividend:%s".formatted(stockSymbol);

        return cacheDataProvider.getOrSet(
            cacheKey,
            Duration.ofDays(1),
            () -> stockRepository.findDividend(stockSymbol)
        );
    }

    /**
     * 取得最後一個交易日的收盤報價資料。
     *
     * @param paginationRequest 分頁請求
     * @return 收盤報價分頁結果
     */
    public PageResult<DailyQuoteDto> getLastDailyQuote(PaginationRequest paginationRequest) {
        String cacheKey = "stock:last-daily-quote:%d:%d".formatted(
            paginationRequest.requestedPage(),
            paginationRequest.recordsPerPage()
        );

        return cacheDataProvider.getOrSet(
            cacheKey,
            Duration.ofMinutes(15),
            () -> stockRepository.findLastDailyQuote(paginationRequest)
        );
    }

    /**
     * 取得最後收盤日。
     *
     * @return 最後收盤日字串
     */
    public String getLastClosingDay() {
        return cacheDataProvider.getOrSet(
            "stock:last-closing-day",
            Duration.ofMinutes(15),
            stockRepository::findLastClosingDay
        );
    }

    /**
     * 取得指定日期的歷史收盤報價資料。
     *
     * @param date 查詢日期
     * @param paginationRequest 分頁請求
     * @return 歷史收盤報價分頁結果
     */
    public PageResult<DailyQuoteDto> getHistoricalDailyQuote(
        LocalDate date,
        PaginationRequest paginationRequest
    ) {
        String cacheKey = "stock:historical-daily-quote:%s:%d:%d".formatted(
            date,
            paginationRequest.requestedPage(),
            paginationRequest.recordsPerPage()
        );

        return cacheDataProvider.getOrSet(
            cacheKey,
            Duration.ofMinutes(15),
            () -> stockRepository.findHistoricalDailyQuote(date, paginationRequest)
        );
    }

    /**
     * 依營收年月查詢營收資料。
     *
     * @param monthOfYear 營收年月
     * @param paginationRequest 分頁請求
     * @return 營收分頁結果
     */
    public PageResult<RevenueDto> getRevenueByMonth(int monthOfYear, PaginationRequest paginationRequest) {
        String cacheKey = "stock:revenue-by-month:%d:%d:%d".formatted(
            monthOfYear,
            paginationRequest.requestedPage(),
            paginationRequest.recordsPerPage()
        );

        return cacheDataProvider.getOrSet(
            cacheKey,
            Duration.ofMinutes(15),
            () -> stockRepository.findRevenue(null, monthOfYear, paginationRequest)
        );
    }

    /**
     * 依股票代號查詢營收資料。
     *
     * @param stockSymbol 股票代號
     * @param paginationRequest 分頁請求
     * @return 營收分頁結果
     */
    public PageResult<RevenueDto> getRevenueByStock(String stockSymbol, PaginationRequest paginationRequest) {
        String cacheKey = "stock:revenue-by-stock:%s:%d:%d".formatted(
            stockSymbol,
            paginationRequest.requestedPage(),
            paginationRequest.recordsPerPage()
        );

        return cacheDataProvider.getOrSet(
            cacheKey,
            Duration.ofMinutes(15),
            () -> stockRepository.findRevenue(stockSymbol, null, paginationRequest)
        );
    }

    /**
     * 取得指定年度的休市日資料。
     *
     * @param year 年度
     * @return 休市日清單
     */
    public List<HolidayScheduleDto> getHolidaySchedule(int year) {
        String cacheKey = "stock:holiday-schedule:%d".formatted(year);

        return cacheDataProvider.getOrSet(
            cacheKey,
            Duration.ofMinutes(15),
            () -> stockGrpcClient.fetchHolidaySchedule(year)
        );
    }
}
