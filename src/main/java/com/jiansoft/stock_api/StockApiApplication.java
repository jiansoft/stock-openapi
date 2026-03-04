package com.jiansoft.stock_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 股票資訊 API 的 Spring Boot 應用程式入口。
 *
 * <p>此類別負責啟動整個後端服務，並啟用設定屬性的掃描機制。
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class StockApiApplication {

    /**
     * 啟動應用程式。
     *
     * @param args 啟動時傳入的命令列參數
     */
    public static void main(String[] args) {
        SpringApplication.run(StockApiApplication.class, args);
    }

}
