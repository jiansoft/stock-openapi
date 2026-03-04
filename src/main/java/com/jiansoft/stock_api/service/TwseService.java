package com.jiansoft.stock_api.service;

import com.jiansoft.stock_api.dto.common.ApiResponse;
import com.jiansoft.stock_api.dto.common.PagingPayload;
import com.jiansoft.stock_api.dto.twse.TaiexDto;
import com.jiansoft.stock_api.provider.TwseDataProvider;
import com.jiansoft.stock_api.support.PaginationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * 台灣證券交易所資料服務。
 */
@Service
public class TwseService {

    private final TwseDataProvider twseDataProvider;

    public TwseService(TwseDataProvider twseDataProvider) {
        this.twseDataProvider = twseDataProvider;
    }

    /**
     * 取得台灣加權股價指數資料。
     *
     * @param requestedPage 欲查詢的頁碼
     * @param recordsPerPage 每頁資料筆數
     * @return 台灣加權股價指數的分頁回應
     */
    public ApiResponse<PagingPayload<TaiexDto>> getTaiex(Integer requestedPage, Integer recordsPerPage) {
        var paginationRequest = PaginationRequest.of(requestedPage, recordsPerPage);
        var result = twseDataProvider.getTaiex(paginationRequest);

        return new ApiResponse<>(
            HttpStatus.OK.value(),
            new PagingPayload<>(result.meta(), result.data())
        );
    }
}
