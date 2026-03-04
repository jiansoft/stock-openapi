package com.jiansoft.stock_api.controller;

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
import com.jiansoft.stock_api.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 股票相關 API 控制器。
 *
 * <p>提供股票基本資料、產業分類、股利、股價與營收等查詢功能。
 */
@Tag(name = "Stock", description = "股票相關查詢")
@RestController
@RequestMapping(path = "/api/stock", produces = MediaType.APPLICATION_JSON_VALUE)
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    /**
     * 取得股票基本資料。
     *
     * @param requestedPage 欲查詢的頁碼
     * @param recordsPerPage 每頁資料筆數
     * @return 股票基本資料的分頁結果
     */
    @Operation(summary = "獲取股票基本資料")
    @GetMapping("/details")
    public ApiResponse<PagingPayload<DetailDto>> details(
        @Parameter(description = "取得第幾頁的資料")
        @RequestParam(required = false) Integer requestedPage,
        @Parameter(description = "每頁幾筆資料")
        @RequestParam(required = false) Integer recordsPerPage
    ) {
        return stockService.getDetails(requestedPage, recordsPerPage);
    }

    /**
     * 取得股票產業分類清單。
     *
     * @return 股票產業分類資料
     */
    @Operation(summary = "取得股票產業分類")
    @GetMapping("/industry")
    public ApiResponse<Payload<List<IndustryDto>>> industries() {
        return stockService.getIndustries();
    }

    /**
     * 依股票代號取得歷年股利資料。
     *
     * @param stockSymbol 股票代號
     * @return 指定股票的股利資料
     */
    @Operation(summary = "取得股票歷年發放股利")
    @GetMapping("/dividend/{stockSymbol}")
    public ApiResponse<Payload<List<DividendDto>>> dividend(
        @Parameter(description = "股票代號")
        @PathVariable String stockSymbol
    ) {
        return stockService.getDividend(stockSymbol);
    }

    /**
     * 取得最後一個交易日的收盤報價資料。
     *
     * @param requestedPage 欲查詢的頁碼
     * @param recordsPerPage 每頁資料筆數
     * @return 最後一個交易日的股價分頁結果
     */
    @Operation(summary = "取得最後的收盤股價")
    @GetMapping("/last_daily_quote")
    public ApiResponse<LastDailyQuotePayload<DailyQuoteDto>> lastDailyQuote(
        @Parameter(description = "取得第幾頁的資料")
        @RequestParam(required = false) Integer requestedPage,
        @Parameter(description = "每頁幾筆資料")
        @RequestParam(required = false) Integer recordsPerPage
    ) {
        return stockService.getLastDailyQuote(requestedPage, recordsPerPage);
    }

    /**
     * 取得指定日期的歷史收盤報價資料。
     *
     * @param date 查詢日期
     * @param requestedPage 欲查詢的頁碼
     * @param recordsPerPage 每頁資料筆數
     * @return 指定日期的股價分頁結果
     */
    @Operation(summary = "取得歷史的收盤股價")
    @GetMapping("/historical_daily_quote/{date}")
    public ApiResponse<PagingPayload<DailyQuoteDto>> historicalDailyQuote(
        @Parameter(description = "日期")
        @PathVariable
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,
        @Parameter(description = "取得第幾頁的資料")
        @RequestParam(required = false) Integer requestedPage,
        @Parameter(description = "每頁幾筆資料")
        @RequestParam(required = false) Integer recordsPerPage
    ) {
        return stockService.getHistoricalDailyQuote(date, requestedPage, recordsPerPage);
    }

    /**
     * 依營收年月查詢營收資料。
     *
     * @param monthOfYear 營收年月，格式通常為 {@code yyyyMM}
     * @param requestedPage 欲查詢的頁碼
     * @param recordsPerPage 每頁資料筆數
     * @return 營收分頁結果
     */
    @Operation(summary = "依營收年月取得營收資料")
    @GetMapping("/revenue_on/{monthOfYear}")
    public ApiResponse<PagingPayload<RevenueDto>> revenueOn(
        @Parameter(description = "日期，格式：yyyyMM")
        @PathVariable int monthOfYear,
        @Parameter(description = "取得第幾頁的資料")
        @RequestParam(required = false) Integer requestedPage,
        @Parameter(description = "每頁幾筆資料")
        @RequestParam(required = false) Integer recordsPerPage
    ) {
        return stockService.getRevenueByMonth(monthOfYear, requestedPage, recordsPerPage);
    }

    /**
     * 依股票代號查詢營收資料。
     *
     * @param stockSymbol 股票代號
     * @param requestedPage 欲查詢的頁碼
     * @param recordsPerPage 每頁資料筆數
     * @return 指定股票的營收分頁結果
     */
    @Operation(summary = "取得營收相關資料")
    @GetMapping("/revenue_by/{stockSymbol}")
    public ApiResponse<PagingPayload<RevenueDto>> revenueBy(
        @Parameter(description = "指定股票")
        @PathVariable String stockSymbol,
        @Parameter(description = "取得第幾頁的資料")
        @RequestParam(required = false) Integer requestedPage,
        @Parameter(description = "每頁幾筆資料")
        @RequestParam(required = false) Integer recordsPerPage
    ) {
        return stockService.getRevenueByStock(stockSymbol, requestedPage, recordsPerPage);
    }

    /**
     * 取得指定年度的休市日資料。
     *
     * @param year 年度
     * @return 該年度休市日清單
     */
    @Operation(summary = "獲取指定年份的休市日期")
    @GetMapping("/holiday_schedule/{year}")
    public ApiResponse<Payload<List<HolidayScheduleDto>>> holidaySchedule(
        @Parameter(description = "指定年份")
        @PathVariable int year
    ) {
        return stockService.getHolidaySchedule(year);
    }
}
