package com.jiansoft.stock_api.dto.common;

import com.jiansoft.stock_api.support.PaginationRequest;

/**
 * 分頁資訊。
 *
 * @param currentPage 目前頁碼
 * @param recordsPerPage 每頁資料筆數
 * @param totalPages 總頁數
 * @param totalRecords 總資料筆數
 */
public record Meta(int currentPage, int recordsPerPage, int totalPages, long totalRecords) {

    private static final int DEFAULT_PAGE = 1;

    /**
     * 依總筆數與分頁請求建立分頁資訊。
     *
     * @param totalRecords 總資料筆數
     * @param paginationRequest 分頁請求
     * @return 計算後的分頁資訊
     */
    public static Meta from(long totalRecords, PaginationRequest paginationRequest) {
        int pageSize = paginationRequest.recordsPerPage();
        int totalPages = totalRecords > 0 ? (int) Math.ceil(totalRecords / (double) pageSize) : 0;
        int currentPage = Math.max(DEFAULT_PAGE, paginationRequest.requestedPage());

        if (totalPages > 0) {
            currentPage = Math.min(currentPage, totalPages);
        } else {
            currentPage = DEFAULT_PAGE;
        }

        return new Meta(currentPage, pageSize, totalPages, totalRecords);
    }

    /**
     * 取得資料庫查詢用的位移量。
     *
     * @return 分頁 offset
     */
    public int offset() {
        return (currentPage - 1) * recordsPerPage;
    }

    /**
     * 取得本次分頁的頁面大小。
     *
     * @return 每頁資料筆數
     */
    public int pageSize() {
        return recordsPerPage;
    }
}
