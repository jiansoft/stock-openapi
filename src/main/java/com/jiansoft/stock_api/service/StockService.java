package com.jiansoft.stock_api.service;

import com.jiansoft.stock_api.dto.common.ApiResponse;
import com.jiansoft.stock_api.dto.common.LastDailyQuotePayload;
import com.jiansoft.stock_api.dto.common.Payload;
import com.jiansoft.stock_api.dto.common.PagingPayload;
import com.jiansoft.stock_api.dto.stock.DailyQuoteDto;
import com.jiansoft.stock_api.dto.stock.DetailDto;
import com.jiansoft.stock_api.dto.stock.DividendDto;
import com.jiansoft.stock_api.dto.stock.HolidayScheduleDto;
import com.jiansoft.stock_api.dto.stock.IndustryDto;
import com.jiansoft.stock_api.dto.stock.RevenueDto;
import com.jiansoft.stock_api.provider.StocksDataProvider;
import com.jiansoft.stock_api.support.PaginationRequest;
import java.time.LocalDate;
import java.util.Locale;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 股票資料服務。
 *
 * <p>負責協調資料庫查詢與 gRPC 呼叫，並整理為 API 回應格式。
 */
@Service
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);
    private static final int HTTP_OK = HttpStatus.OK.value();
    private static final int MIN_QUERY_YEAR = 1900;
    private static final int MAX_QUERY_YEAR = 2100;
    private static final LocalDate MIN_HISTORY_DATE = LocalDate.of(MIN_QUERY_YEAR, 1, 1);
    private static final Pattern STOCK_SYMBOL_PATTERN = Pattern.compile("^[0-9A-Za-z]{1,12}$");

    private final StocksDataProvider stocksDataProvider;

    public StockService(StocksDataProvider stocksDataProvider) {
        this.stocksDataProvider = stocksDataProvider;
    }

    /**
     * 取得股票基本資料。
     *
     * @param requestedPage 欲查詢的頁碼
     * @param recordsPerPage 每頁資料筆數
     * @return 股票基本資料的分頁回應
     */
    public ApiResponse<PagingPayload<DetailDto>> getDetails(Integer requestedPage, Integer recordsPerPage) {
        logger.debug(
            "查詢股票基本資料，requestedPage={}, recordsPerPage={}",
            requestedPage,
            recordsPerPage
        );
        var paginationRequest = PaginationRequest.of(requestedPage, recordsPerPage);
        var result = stocksDataProvider.getDetails(paginationRequest);

        return new ApiResponse<>(
            HTTP_OK,
            new PagingPayload<>(result.meta(), result.data())
        );
    }

    /**
     * 取得股票產業分類。
     *
     * @return 股票產業分類回應
     */
    public ApiResponse<Payload<List<IndustryDto>>> getIndustries() {
        return new ApiResponse<>(
            HTTP_OK,
            new Payload<>(stocksDataProvider.getIndustries())
        );
    }

    /**
     * 取得指定股票的股利資料。
     *
     * @param stockSymbol 股票代號
     * @return 股利資料回應
     */
    public ApiResponse<Payload<List<DividendDto>>> getDividend(String stockSymbol) {
        String normalizedStockSymbol = normalizeStockSymbol(stockSymbol);

        return new ApiResponse<>(
            HTTP_OK,
            new Payload<>(stocksDataProvider.getDividend(normalizedStockSymbol))
        );
    }

    /**
     * 取得最後一個交易日的收盤報價資料。
     *
     * @param requestedPage 欲查詢的頁碼
     * @param recordsPerPage 每頁資料筆數
     * @return 最後一個交易日報價回應
     */
    public ApiResponse<LastDailyQuotePayload<DailyQuoteDto>> getLastDailyQuote(
        Integer requestedPage,
        Integer recordsPerPage
    ) {
        var paginationRequest = PaginationRequest.of(requestedPage, recordsPerPage);
        var result = stocksDataProvider.getLastDailyQuote(paginationRequest);

        return new ApiResponse<>(
            HTTP_OK,
            new LastDailyQuotePayload<>(
                stocksDataProvider.getLastClosingDay(),
                result.meta(),
                result.data()
            )
        );
    }

    /**
     * 取得指定日期的歷史收盤報價資料。
     *
     * @param date 查詢日期
     * @param requestedPage 欲查詢的頁碼
     * @param recordsPerPage 每頁資料筆數
     * @return 歷史收盤報價回應
     */
    public ApiResponse<PagingPayload<DailyQuoteDto>> getHistoricalDailyQuote(
        LocalDate date,
        Integer requestedPage,
        Integer recordsPerPage
    ) {
        validateHistoricalDate(date);
        var paginationRequest = PaginationRequest.of(requestedPage, recordsPerPage);
        var result = stocksDataProvider.getHistoricalDailyQuote(date, paginationRequest);

        return new ApiResponse<>(
            HTTP_OK,
            new PagingPayload<>(result.meta(), result.data())
        );
    }

    /**
     * 依營收年月查詢營收資料。
     *
     * @param monthOfYear 營收年月
     * @param requestedPage 欲查詢的頁碼
     * @param recordsPerPage 每頁資料筆數
     * @return 營收分頁回應
     */
    public ApiResponse<PagingPayload<RevenueDto>> getRevenueByMonth(
        int monthOfYear,
        Integer requestedPage,
        Integer recordsPerPage
    ) {
        validateMonthOfYear(monthOfYear);
        var paginationRequest = PaginationRequest.of(requestedPage, recordsPerPage);
        var result = stocksDataProvider.getRevenueByMonth(monthOfYear, paginationRequest);

        return new ApiResponse<>(
            HTTP_OK,
            new PagingPayload<>(result.meta(), result.data())
        );
    }

    /**
     * 依股票代號查詢營收資料。
     *
     * @param stockSymbol 股票代號
     * @param requestedPage 欲查詢的頁碼
     * @param recordsPerPage 每頁資料筆數
     * @return 營收分頁回應
     */
    public ApiResponse<PagingPayload<RevenueDto>> getRevenueByStock(
        String stockSymbol,
        Integer requestedPage,
        Integer recordsPerPage
    ) {
        String normalizedStockSymbol = normalizeStockSymbol(stockSymbol);
        var paginationRequest = PaginationRequest.of(requestedPage, recordsPerPage);
        var result = stocksDataProvider.getRevenueByStock(normalizedStockSymbol, paginationRequest);

        return new ApiResponse<>(
            HTTP_OK,
            new PagingPayload<>(result.meta(), result.data())
        );
    }

    /**
     * 取得指定年度的休市日資料。
     *
     * @param year 年度
     * @return 休市日資料回應
     */
    public ApiResponse<Payload<List<HolidayScheduleDto>>> getHolidaySchedule(int year) {
        validateYear(year);
        logger.info("查詢 {} 年休市日資料", year);

        return new ApiResponse<>(
            HTTP_OK,
            new Payload<>(stocksDataProvider.getHolidaySchedule(year))
        );
    }

    private String normalizeStockSymbol(String stockSymbol) {
        if (!StringUtils.hasText(stockSymbol)) {
            throw new IllegalArgumentException("stockSymbol must not be blank.");
        }

        String normalizedStockSymbol = stockSymbol.strip().toUpperCase(Locale.ROOT);
        if (!STOCK_SYMBOL_PATTERN.matcher(normalizedStockSymbol).matches()) {
            throw new IllegalArgumentException("stockSymbol format is invalid.");
        }

        return normalizedStockSymbol;
    }

    private void validateHistoricalDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date must not be null.");
        }
        if (date.isBefore(MIN_HISTORY_DATE)) {
            throw new IllegalArgumentException("date must not be earlier than 1900-01-01.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("date must not be in the future.");
        }
    }

    private void validateMonthOfYear(int monthOfYear) {
        int year = monthOfYear / 100;
        int month = monthOfYear % 100;
        if (year < MIN_QUERY_YEAR || year > MAX_QUERY_YEAR || month < 1 || month > 12) {
            throw new IllegalArgumentException("monthOfYear must be in yyyyMM format and within 190001-210012.");
        }
    }

    private void validateYear(int year) {
        if (year < MIN_QUERY_YEAR || year > MAX_QUERY_YEAR) {
            throw new IllegalArgumentException("year must be within 1900-2100.");
        }
    }
}
