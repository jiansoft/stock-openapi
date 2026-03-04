package com.jiansoft.stock_api.support;

import com.jiansoft.stock_api.dto.common.Meta;
import java.util.List;

/**
 * 分頁查詢結果。
 *
 * @param <T> 資料項目的型別
 * @param meta 分頁資訊
 * @param data 實際資料清單
 */
public record PageResult<T>(Meta meta, List<T> data) {
}
