package com.jiansoft.stock_api.controller;

import com.jiansoft.stock_api.dto.common.ApiResponse;
import com.jiansoft.stock_api.dto.common.PagingPayload;
import com.jiansoft.stock_api.dto.twse.TaiexDto;
import com.jiansoft.stock_api.service.TwseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 台灣證券交易所相關 API 控制器。
 */
@Tag(name = "Twse", description = "台灣證券交易所相關查詢")
@RestController
@RequestMapping(path = "/api/twse", produces = MediaType.APPLICATION_JSON_VALUE)
public class TwseController {

    private final TwseService twseService;

    public TwseController(TwseService twseService) {
        this.twseService = twseService;
    }

    /**
     * 取得台灣加權股價指數資料。
     *
     * @param requestedPage 欲查詢的頁碼
     * @param recordsPerPage 每頁資料筆數
     * @return 台灣加權股價指數的分頁結果
     */
    @Operation(summary = "獲取台灣加權股價指數（TAIEX）的資料")
    @GetMapping("/taiex")
    public ApiResponse<PagingPayload<TaiexDto>> taiex(
        @Parameter(description = "請求的頁碼，指定要取得第幾頁的資料")
        @RequestParam(required = false) Integer requestedPage,
        @Parameter(description = "每頁資料筆數，指定每頁顯示多少筆資料")
        @RequestParam(required = false) Integer recordsPerPage
    ) {
        return twseService.getTaiex(requestedPage, recordsPerPage);
    }
}
