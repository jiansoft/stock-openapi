package com.jiansoft.stock_api.config;

import com.zaxxer.hikari.HikariDataSource;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 資料來源設定。
 *
 * <p>將原始 C# 專案使用的 Npgsql 風格連線字串轉為 Java 可使用的 JDBC 資料來源。
 */
@Configuration
public class DataSourceConfig {

    /**
     * 建立應用程式使用的資料來源。
     *
     * @param databaseProperties 資料庫設定
     * @return 可供 Spring JDBC 使用的資料來源
     */
    @Bean
    public DataSource dataSource(DatabaseProperties databaseProperties) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setMaximumPoolSize(databaseProperties.getMaximumPoolSize());
        dataSource.setInitializationFailTimeout(databaseProperties.getInitializationFailTimeout());

        String connection = databaseProperties.getConnection();
        if (!StringUtils.hasText(connection)) {
            throw new IllegalStateException("app.database.connection must not be blank.");
        }

        if (connection.startsWith("jdbc:")) {
            dataSource.setJdbcUrl(connection);
            String driverClassName = inferDriverClassName(connection);
            if (driverClassName != null) {
                dataSource.setDriverClassName(driverClassName);
            }

            return dataSource;
        }

        Map<String, String> tokens = parseConnectionString(connection);
        String host = firstNonBlank(tokens, "server", "host", "hostname");
        String port = firstNonBlank(tokens, "port");
        String database = firstNonBlank(tokens, "database", "initial catalog");
        String username = firstNonBlank(tokens, "uid", "user id", "user", "username");
        String password = firstNonBlank(tokens, "pwd", "password");

        if (!StringUtils.hasText(host) || !StringUtils.hasText(database)) {
            throw new IllegalStateException("Database connection string must include server and database.");
        }

        String jdbcUrl = StringUtils.hasText(port)
            ? "jdbc:postgresql://%s:%s/%s".formatted(host, port, database)
            : "jdbc:postgresql://%s/%s".formatted(host, database);

        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setDriverClassName("org.postgresql.Driver");

        if (StringUtils.hasText(username)) {
            dataSource.setUsername(username);
        }
        if (password != null) {
            dataSource.setPassword(password);
        }

        return dataSource;
    }

    private Map<String, String> parseConnectionString(String connection) {
        Map<String, String> tokens = new LinkedHashMap<>();
        String[] segments = connection.split(";");

        for (String segment : segments) {
            if (!StringUtils.hasText(segment)) {
                continue;
            }

            int separatorIndex = segment.indexOf('=');
            if (separatorIndex <= 0) {
                continue;
            }

            String key = segment.substring(0, separatorIndex).trim().toLowerCase(Locale.ROOT);
            String value = segment.substring(separatorIndex + 1).trim();
            tokens.put(key, value);
        }

        return tokens;
    }

    private String firstNonBlank(Map<String, String> tokens, String... keys) {
        for (String key : keys) {
            String value = tokens.get(key);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }

        return null;
    }

    private String inferDriverClassName(String connection) {
        if (connection.startsWith("jdbc:h2:")) {
            return "org.h2.Driver";
        }
        if (connection.startsWith("jdbc:postgresql:")) {
            return "org.postgresql.Driver";
        }

        return null;
    }
}
