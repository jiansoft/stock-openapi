package com.jiansoft.stock_api.provider;

import com.jiansoft.stock_api.dto.twse.TaiexDto;
import com.jiansoft.stock_api.repository.TwseRepository;
import com.jiansoft.stock_api.support.CacheDataProvider;
import com.jiansoft.stock_api.support.PageResult;
import com.jiansoft.stock_api.support.PaginationRequest;
import java.time.Duration;
import org.springframework.stereotype.Component;

/**
 * 台灣證券交易所資料提供者。
 *
 * <p>集中處理 TWSE 相關資料查詢與快取。
 */
@Component
public class TwseDataProvider extends DbDataProvider {

    private final TwseRepository twseRepository;

    public TwseDataProvider(CacheDataProvider cacheDataProvider, TwseRepository twseRepository) {
        super(cacheDataProvider);
        this.twseRepository = twseRepository;
    }

    /**
     * 取得台灣加權股價指數資料。
     *
     * @param paginationRequest 分頁請求
     * @return 台灣加權股價指數分頁結果
     */
    public PageResult<TaiexDto> getTaiex(PaginationRequest paginationRequest) {
        String cacheKey = "twse:taiex:%d:%d".formatted(
            paginationRequest.requestedPage(),
            paginationRequest.recordsPerPage()
        );

        return cacheDataProvider.getOrSet(
            cacheKey,
            Duration.ofMinutes(15),
            () -> twseRepository.findTaiex(paginationRequest)
        );
    }
}
