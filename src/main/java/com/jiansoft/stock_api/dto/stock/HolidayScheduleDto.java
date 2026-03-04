package com.jiansoft.stock_api.dto.stock;

/**
 * 休市日資料。
 *
 * @param date 休市日期
 * @param why 休市原因
 */
public record HolidayScheduleDto(String date, String why) {
}
