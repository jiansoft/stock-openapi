package com.jiansoft.stock_api.support;

/**
 * 分頁請求參數。
 *
 * @param requestedPage 請求的頁碼，從 1 開始
 * @param recordsPerPage 每頁資料筆數
 */
public record PaginationRequest(int requestedPage, int recordsPerPage) {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_RECORDS_PER_PAGE = 30;
    private static final int MAXIMUM_RECORDS_PER_PAGE = 1000;

    /**
     * 建立已正規化的分頁請求。
     *
     * <p>當頁碼或每頁筆數不合法時，會自動套用系統預設值。
     *
     * @param requestedPage 使用者傳入的頁碼
     * @param recordsPerPage 使用者傳入的每頁筆數
     * @return 正規化後的分頁請求
     */
    public static PaginationRequest of(Integer requestedPage, Integer recordsPerPage) {
        int normalizedPage = requestedPage == null || requestedPage <= 0
            ? DEFAULT_PAGE
            : requestedPage;
        int normalizedRecordsPerPage = recordsPerPage == null || recordsPerPage <= 0
            ? DEFAULT_RECORDS_PER_PAGE
            : Math.clamp(recordsPerPage.longValue(), 1, MAXIMUM_RECORDS_PER_PAGE);

        return new PaginationRequest(normalizedPage, normalizedRecordsPerPage);
    }
}
