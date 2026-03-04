package com.jiansoft.stock_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 資料庫連線設定。
 *
 * <p>此設定對應到 {@code application.yaml} 中的 {@code app.database} 區段，
 * 支援使用與原始 C# 專案相同的單一連線字串格式。
 */
@ConfigurationProperties(prefix = "app.database")
public class DatabaseProperties {

    private String connection = "";
    private Integer maximumPoolSize = 5;
    private Long initializationFailTimeout = -1L;

    /**
     * 取得資料庫連線字串。
     *
     * @return 資料庫連線字串
     */
    public String getConnection() {
        return connection;
    }

    /**
     * 設定資料庫連線字串。
     *
     * @param connection 資料庫連線字串
     */
    public void setConnection(String connection) {
        this.connection = connection;
    }

    /**
     * 取得連線池最大連線數。
     *
     * @return 連線池最大連線數
     */
    public Integer getMaximumPoolSize() {
        return maximumPoolSize;
    }

    /**
     * 設定連線池最大連線數。
     *
     * @param maximumPoolSize 連線池最大連線數
     */
    public void setMaximumPoolSize(Integer maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    /**
     * 取得初始化失敗逾時時間。
     *
     * @return 初始化失敗逾時時間（毫秒）
     */
    public Long getInitializationFailTimeout() {
        return initializationFailTimeout;
    }

    /**
     * 設定初始化失敗逾時時間。
     *
     * @param initializationFailTimeout 初始化失敗逾時時間（毫秒）
     */
    public void setInitializationFailTimeout(Long initializationFailTimeout) {
        this.initializationFailTimeout = initializationFailTimeout;
    }
}
