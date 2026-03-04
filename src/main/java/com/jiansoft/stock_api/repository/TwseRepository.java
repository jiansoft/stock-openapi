package com.jiansoft.stock_api.repository;

import com.jiansoft.stock_api.dto.common.Meta;
import com.jiansoft.stock_api.dto.twse.TaiexDto;
import com.jiansoft.stock_api.support.PageResult;
import com.jiansoft.stock_api.support.PaginationRequest;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 台灣證券交易所資料存取層。
 */
@Repository
public class TwseRepository {

    private static final String TAIEX_CATEGORY = "TAIEX";

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public TwseRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 查詢台灣加權股價指數資料。
     *
     * @param paginationRequest 分頁請求
     * @return 台灣加權股價指數的分頁結果
     */
    public PageResult<TaiexDto> findTaiex(PaginationRequest paginationRequest) {
        var params = new MapSqlParameterSource("category", TAIEX_CATEGORY);
        Long totalRecords = jdbcTemplate.queryForObject("""
            select count(*)
            from "index"
            where category = :category
            """, params, Long.class);

        Meta meta = Meta.from(totalRecords == null ? 0L : totalRecords, paginationRequest);
        if (totalRecords == null || totalRecords == 0) {
            return new PageResult<>(meta, List.of());
        }

        params.addValue("offset", meta.offset());
        params.addValue("limit", meta.pageSize());

        List<TaiexDto> rows = jdbcTemplate.query("""
            select
                date,
                trading_volume,
                transaction,
                trade_value,
                change,
                index
            from "index"
            where category = :category
            order by date desc
            offset :offset limit :limit
            """, params, this::mapTaiex);

        return new PageResult<>(meta, rows);
    }

    /**
     * 將資料列轉為台灣加權股價指數 DTO。
     *
     * @param resultSet 查詢結果
     * @param rowNum 目前列號
     * @return 台灣加權股價指數 DTO
     * @throws SQLException 當欄位讀取失敗時拋出
     */
    private TaiexDto mapTaiex(ResultSet resultSet, int rowNum) throws SQLException {
        return new TaiexDto(
            toLocalDate(resultSet, "date"),
            bigDecimal(resultSet, "trading_volume"),
            bigDecimal(resultSet, "transaction"),
            bigDecimal(resultSet, "trade_value"),
            bigDecimal(resultSet, "change"),
            bigDecimal(resultSet, "index")
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
