package com.jiansoft.stock_api.repository;

import com.jiansoft.stock_api.dto.common.Meta;
import com.jiansoft.stock_api.dto.stock.DailyQuoteDto;
import com.jiansoft.stock_api.dto.stock.DetailDto;
import com.jiansoft.stock_api.dto.stock.DividendDto;
import com.jiansoft.stock_api.dto.stock.IndustryDto;
import com.jiansoft.stock_api.dto.stock.RevenueDto;
import com.jiansoft.stock_api.support.PageResult;
import com.jiansoft.stock_api.support.PaginationRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * 股票資料存取層。
 *
 * <p>集中處理股票相關 SQL 查詢，並將查詢結果轉為 DTO。
 */
@Repository
public class StockRepository {

    private static final List<Integer> EXCHANGE_MARKETS = List.of(2, 4, 5);

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public StockRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 查詢股票基本資料。
     *
     * @param paginationRequest 分頁請求
     * @return 股票基本資料的分頁結果
     */
    public PageResult<DetailDto> findDetails(PaginationRequest paginationRequest) {
        var params = new MapSqlParameterSource("exchangeMarkets", EXCHANGE_MARKETS);
        long totalRecords = queryForCount("""
            select count(*)
            from stocks
            where stock_exchange_market_id in (:exchangeMarkets)
            """, params);

        Meta meta = Meta.from(totalRecords, paginationRequest);
        if (totalRecords == 0) {
            return new PageResult<>(meta, List.of());
        }

        params.addValue("offset", meta.offset());
        params.addValue("limit", meta.pageSize());

        List<DetailDto> rows = jdbcTemplate.query("""
            select
                stock_symbol,
                "Name" as name,
                last_one_eps,
                last_four_eps,
                weight,
                stock_industry_id,
                stock_exchange_market_id
            from stocks
            where stock_exchange_market_id in (:exchangeMarkets)
            order by stock_exchange_market_id, stock_industry_id, stock_symbol
            offset :offset limit :limit
            """, params, this::mapDetail);

        return new PageResult<>(meta, rows);
    }

    /**
     * 查詢股票產業分類清單。
     *
     * @return 股票產業分類清單
     */
    public List<IndustryDto> findIndustries() {
        return jdbcTemplate.query("""
            select
                stock_industry_id,
                name
            from stock_industry
            order by stock_industry_id
            """, new MapSqlParameterSource(), this::mapIndustry);
    }

    /**
     * 依股票代號查詢歷年股利資料。
     *
     * @param stockSymbol 股票代號
     * @return 股利資料清單
     */
    public List<DividendDto> findDividend(String stockSymbol) {
        var params = new MapSqlParameterSource("stockSymbol", stockSymbol);

        return jdbcTemplate.query("""
            select
                d.security_code as stock_symbol,
                d.year as year,
                d.year_of_dividend as year_of_dividend,
                d.quarter as quarter,
                d.cash_dividend as cash_dividend,
                d.stock_dividend as stock_dividend,
                d.sum as sum,
                coalesce(fs.earnings_per_share, 0) as earnings_per_share,
                d."ex-dividend_date1" as ex_dividend_date1,
                d."ex-dividend_date2" as ex_dividend_date2,
                d.payable_date1 as payable_date1,
                d.payable_date2 as payable_date2,
                d.payout_ratio as payout_ratio,
                coalesce(d.cash_dividend / dq."ClosingPrice" * 100, 0) as cash_dividend_yield
            from dividend d
            left join financial_statement fs
                on d.security_code = fs.security_code
                and d.year_of_dividend = fs.year
                and d.quarter = fs.quarter
            left join lateral (
                select
                    "Date",
                    "ClosingPrice"
                from "DailyQuotes" dq
                where dq."SecurityCode" = d.security_code
                    and d."ex-dividend_date1" != '-'
                    and d."ex-dividend_date1" != '尚未公布'
                    and dq."Date" < to_date(d."ex-dividend_date1", 'YYYY-MM-DD')
                order by "Date" desc
                limit 1
            ) dq on true
            where d.security_code = :stockSymbol
            order by d.year desc, d.year_of_dividend desc, d.quarter desc
            """, params, this::mapDividend);
    }

    /**
     * 查詢最後一個交易日的收盤報價資料。
     *
     * @param paginationRequest 分頁請求
     * @return 收盤報價的分頁結果
     */
    public PageResult<DailyQuoteDto> findLastDailyQuote(PaginationRequest paginationRequest) {
        long totalRecords = queryForCount("""
            select count(*)
            from stock_exchange_market sem
            inner join stocks s on s.stock_exchange_market_id = sem.stock_exchange_market_id
            inner join last_daily_quotes ldq on s.stock_symbol = ldq.stock_symbol
            where s."SuspendListing" = false
            """, new MapSqlParameterSource());

        Meta meta = Meta.from(totalRecords, paginationRequest);
        if (totalRecords == 0) {
            return new PageResult<>(meta, List.of());
        }

        var params = new MapSqlParameterSource()
            .addValue("offset", meta.offset())
            .addValue("limit", meta.pageSize());

        List<DailyQuoteDto> rows = jdbcTemplate.query("""
            select
                sem.stock_exchange_market_id,
                s.stock_symbol,
                ldq.trading_volume,
                ldq."transaction" as transaction,
                ldq.trade_value,
                ldq.opening_price,
                ldq.highest_price,
                ldq.lowest_price,
                ldq.closing_price,
                ldq.change_range,
                ldq.change,
                ldq.moving_average_5,
                ldq.moving_average_10,
                ldq.moving_average_20,
                ldq.moving_average_60,
                ldq.moving_average_120,
                ldq.moving_average_240,
                ldq.maximum_price_in_year,
                ldq.minimum_price_in_year,
                ldq.average_price_in_year,
                ldq.maximum_price_in_year_date_on,
                ldq.minimum_price_in_year_date_on,
                ldq."price-to-book_ratio" as price_to_book_ratio
            from stock_exchange_market sem
            inner join stocks s on s.stock_exchange_market_id = sem.stock_exchange_market_id
            inner join last_daily_quotes ldq on s.stock_symbol = ldq.stock_symbol
            where s."SuspendListing" = false
            order by s.stock_symbol
            offset :offset limit :limit
            """, params, this::mapDailyQuote);

        return new PageResult<>(meta, rows);
    }

    /**
     * 查詢最後收盤日字串。
     *
     * @return 最後收盤日；若查無資料則回傳空字串
     */
    public String findLastClosingDay() {
        try {
            String lastClosingDay = jdbcTemplate.queryForObject("""
                select val
                from config
                where key = :key
                """, new MapSqlParameterSource("key", "last-closing-day"), String.class);

            return lastClosingDay == null ? "" : lastClosingDay;
        } catch (EmptyResultDataAccessException ex) {
            return "";
        }
    }

    /**
     * 查詢指定日期的歷史收盤報價資料。
     *
     * @param date 查詢日期
     * @param paginationRequest 分頁請求
     * @return 歷史收盤報價的分頁結果
     */
    public PageResult<DailyQuoteDto> findHistoricalDailyQuote(LocalDate date, PaginationRequest paginationRequest) {
        var baseParams = new MapSqlParameterSource("date", Date.valueOf(date));
        long totalRecords = queryForCount("""
            select count(*)
            from stock_exchange_market sem
            inner join stocks s on s.stock_exchange_market_id = sem.stock_exchange_market_id
            inner join "DailyQuotes" dq on s.stock_symbol = dq."SecurityCode"
            where dq."Date" = :date
                and s."SuspendListing" = false
            """, baseParams);

        Meta meta = Meta.from(totalRecords, paginationRequest);
        if (totalRecords == 0) {
            return new PageResult<>(meta, List.of());
        }

        var params = new MapSqlParameterSource("date", Date.valueOf(date))
            .addValue("offset", meta.offset())
            .addValue("limit", meta.pageSize());

        List<DailyQuoteDto> rows = jdbcTemplate.query("""
            select
                sem.stock_exchange_market_id,
                s.stock_symbol,
                dq."TradingVolume" as trading_volume,
                dq."Transaction" as transaction,
                dq."TradeValue" as trade_value,
                dq."OpeningPrice" as opening_price,
                dq."HighestPrice" as highest_price,
                dq."LowestPrice" as lowest_price,
                dq."ClosingPrice" as closing_price,
                dq."ChangeRange" as change_range,
                dq."Change" as change,
                dq."MovingAverage5" as moving_average_5,
                dq."MovingAverage10" as moving_average_10,
                dq."MovingAverage20" as moving_average_20,
                dq."MovingAverage60" as moving_average_60,
                dq."MovingAverage120" as moving_average_120,
                dq."MovingAverage240" as moving_average_240,
                dq.maximum_price_in_year,
                dq.minimum_price_in_year,
                dq.average_price_in_year,
                dq.maximum_price_in_year_date_on,
                dq.minimum_price_in_year_date_on,
                dq."price-to-book_ratio" as price_to_book_ratio
            from stock_exchange_market sem
            inner join stocks s on s.stock_exchange_market_id = sem.stock_exchange_market_id
            inner join "DailyQuotes" dq on s.stock_symbol = dq."SecurityCode"
            where dq."Date" = :date
                and s."SuspendListing" = false
            order by s.stock_symbol
            offset :offset limit :limit
            """, params, this::mapDailyQuote);

        return new PageResult<>(meta, rows);
    }

    /**
     * 查詢營收資料。
     *
     * <p>可依股票代號、營收年月或兩者組合進行過濾。
     *
     * @param stockSymbol 股票代號，可為 {@code null}
     * @param monthOfYear 營收年月，可為 {@code null}
     * @param paginationRequest 分頁請求
     * @return 營收分頁結果
     */
    public PageResult<RevenueDto> findRevenue(
        String stockSymbol,
        Integer monthOfYear,
        PaginationRequest paginationRequest
    ) {
        var countSql = new StringBuilder("""
            select count(*)
            from "Revenue"
            where 1 = 1
            """);
        var querySql = new StringBuilder("""
            select
                "SecurityCode" as security_code,
                "Date" as date,
                "Monthly" as monthly,
                "LastMonth" as last_month,
                "LastYearThisMonth" as last_year_this_month,
                "MonthlyAccumulated" as monthly_accumulated,
                "LastYearMonthlyAccumulated" as last_year_monthly_accumulated,
                "ComparedWithLastMonth" as compared_with_last_month,
                "ComparedWithLastYearSameMonth" as compared_with_last_year_same_month,
                "AccumulatedComparedWithLastYear" as accumulated_compared_with_last_year,
                avg_price,
                lowest_price,
                highest_price
            from "Revenue"
            where 1 = 1
            """);
        var params = new MapSqlParameterSource();

        if (StringUtils.hasText(stockSymbol)) {
            countSql.append(" and \"SecurityCode\" = :stockSymbol");
            querySql.append(" and \"SecurityCode\" = :stockSymbol");
            params.addValue("stockSymbol", stockSymbol);
        }

        if (monthOfYear != null && monthOfYear > 0) {
            countSql.append(" and \"Date\" = :monthOfYear");
            querySql.append(" and \"Date\" = :monthOfYear");
            params.addValue("monthOfYear", monthOfYear);
        }

        long totalRecords = queryForCount(countSql.toString(), params);
        Meta meta = Meta.from(totalRecords, paginationRequest);
        if (totalRecords == 0) {
            return new PageResult<>(meta, List.of());
        }

        querySql.append("""
             order by "Date" desc, "SecurityCode"
             offset :offset limit :limit
            """);
        params.addValue("offset", meta.offset());
        params.addValue("limit", meta.pageSize());

        List<RevenueDto> rows = jdbcTemplate.query(querySql.toString(), params, this::mapRevenue);
        return new PageResult<>(meta, rows);
    }

    /**
     * 執行筆數查詢。
     *
     * @param sql 查詢語法
     * @param params 命名參數
     * @return 總筆數
     */
    private long queryForCount(String sql, MapSqlParameterSource params) {
        Long totalRecords = jdbcTemplate.queryForObject(sql, params, Long.class);
        return totalRecords == null ? 0L : totalRecords;
    }

    /**
     * 將資料列轉為股票基本資料 DTO。
     *
     * @param resultSet 查詢結果
     * @param rowNum 目前列號
     * @return 股票基本資料 DTO
     * @throws SQLException 當欄位讀取失敗時拋出
     */
    private DetailDto mapDetail(ResultSet resultSet, int rowNum) throws SQLException {
        return new DetailDto(
            resultSet.getString("stock_symbol"),
            resultSet.getString("name"),
            bigDecimal(resultSet, "last_one_eps"),
            bigDecimal(resultSet, "last_four_eps"),
            bigDecimal(resultSet, "weight"),
            resultSet.getInt("stock_industry_id"),
            resultSet.getInt("stock_exchange_market_id")
        );
    }

    /**
     * 將資料列轉為產業分類 DTO。
     *
     * @param resultSet 查詢結果
     * @param rowNum 目前列號
     * @return 產業分類 DTO
     * @throws SQLException 當欄位讀取失敗時拋出
     */
    private IndustryDto mapIndustry(ResultSet resultSet, int rowNum) throws SQLException {
        return new IndustryDto(
            resultSet.getInt("stock_industry_id"),
            resultSet.getString("name")
        );
    }

    /**
     * 將資料列轉為股利 DTO。
     *
     * @param resultSet 查詢結果
     * @param rowNum 目前列號
     * @return 股利 DTO
     * @throws SQLException 當欄位讀取失敗時拋出
     */
    private DividendDto mapDividend(ResultSet resultSet, int rowNum) throws SQLException {
        return new DividendDto(
            resultSet.getString("ex_dividend_date1"),
            resultSet.getString("ex_dividend_date2"),
            resultSet.getString("payable_date1"),
            resultSet.getString("payable_date2"),
            resultSet.getString("stock_symbol"),
            resultSet.getString("quarter"),
            resultSet.getLong("year"),
            resultSet.getLong("year_of_dividend"),
            bigDecimal(resultSet, "sum"),
            bigDecimal(resultSet, "stock_dividend"),
            bigDecimal(resultSet, "cash_dividend"),
            bigDecimal(resultSet, "payout_ratio"),
            bigDecimal(resultSet, "cash_dividend_yield").setScale(2, RoundingMode.HALF_UP),
            bigDecimal(resultSet, "earnings_per_share")
        );
    }

    /**
     * 將資料列轉為每日報價 DTO。
     *
     * @param resultSet 查詢結果
     * @param rowNum 目前列號
     * @return 每日報價 DTO
     * @throws SQLException 當欄位讀取失敗時拋出
     */
    private DailyQuoteDto mapDailyQuote(ResultSet resultSet, int rowNum) throws SQLException {
        return new DailyQuoteDto(
            resultSet.getInt("stock_exchange_market_id"),
            resultSet.getString("stock_symbol"),
            bigDecimal(resultSet, "trading_volume"),
            bigDecimal(resultSet, "transaction").intValue(),
            bigDecimal(resultSet, "trade_value"),
            bigDecimal(resultSet, "opening_price"),
            bigDecimal(resultSet, "highest_price"),
            bigDecimal(resultSet, "lowest_price"),
            bigDecimal(resultSet, "closing_price"),
            bigDecimal(resultSet, "change_range"),
            bigDecimal(resultSet, "change"),
            bigDecimal(resultSet, "moving_average_5"),
            bigDecimal(resultSet, "moving_average_10"),
            bigDecimal(resultSet, "moving_average_20"),
            bigDecimal(resultSet, "moving_average_60"),
            bigDecimal(resultSet, "moving_average_120"),
            bigDecimal(resultSet, "moving_average_240"),
            bigDecimal(resultSet, "maximum_price_in_year"),
            bigDecimal(resultSet, "minimum_price_in_year"),
            bigDecimal(resultSet, "average_price_in_year"),
            toLocalDate(resultSet, "maximum_price_in_year_date_on"),
            toLocalDate(resultSet, "minimum_price_in_year_date_on"),
            bigDecimal(resultSet, "price_to_book_ratio")
        );
    }

    /**
     * 將資料列轉為營收 DTO。
     *
     * @param resultSet 查詢結果
     * @param rowNum 目前列號
     * @return 營收 DTO
     * @throws SQLException 當欄位讀取失敗時拋出
     */
    private RevenueDto mapRevenue(ResultSet resultSet, int rowNum) throws SQLException {
        return new RevenueDto(
            resultSet.getString("security_code"),
            resultSet.getLong("date"),
            bigDecimal(resultSet, "monthly"),
            bigDecimal(resultSet, "last_month"),
            bigDecimal(resultSet, "last_year_this_month"),
            bigDecimal(resultSet, "monthly_accumulated"),
            bigDecimal(resultSet, "last_year_monthly_accumulated"),
            bigDecimal(resultSet, "compared_with_last_month"),
            bigDecimal(resultSet, "compared_with_last_year_same_month"),
            bigDecimal(resultSet, "accumulated_compared_with_last_year"),
            bigDecimal(resultSet, "avg_price"),
            bigDecimal(resultSet, "lowest_price"),
            bigDecimal(resultSet, "highest_price")
        );
    }

    /**
     * 讀取 {@link BigDecimal} 欄位，若資料庫值為 {@code null} 則回傳 {@link BigDecimal#ZERO}。
     *
     * @param resultSet 查詢結果
     * @param columnName 欄位名稱
     * @return 欄位數值
     * @throws SQLException 當欄位讀取失敗時拋出
     */
    private BigDecimal bigDecimal(ResultSet resultSet, String columnName) throws SQLException {
        BigDecimal value = resultSet.getBigDecimal(columnName);
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 讀取日期欄位並轉為 {@link LocalDate}。
     *
     * @param resultSet 查詢結果
     * @param columnName 欄位名稱
     * @return 日期欄位；若資料庫值為 {@code null} 則回傳 {@code null}
     * @throws SQLException 當欄位讀取失敗時拋出
     */
    private LocalDate toLocalDate(ResultSet resultSet, String columnName) throws SQLException {
        Date value = resultSet.getDate(columnName);
        return value == null ? null : value.toLocalDate();
    }
}
