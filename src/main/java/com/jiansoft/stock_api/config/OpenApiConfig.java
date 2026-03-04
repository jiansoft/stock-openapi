package com.jiansoft.stock_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文件設定。
 *
 * <p>此設定會建立 Swagger 文件的標題、版本、說明與聯絡資訊，
 * 並盡量對齊原始 C# 專案既有的 Swagger 內容。
 */
@Configuration
public class OpenApiConfig {

    /**
     * 建立 OpenAPI 文件描述資訊。
     *
     * @return OpenAPI 設定物件
     */
    @Bean
    public OpenAPI stockApiOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("股票資訊服務")
                .description("本平臺提供歷年台股資訊服務，歡迎各位介接使用。")
                .version("v1.0.1")
                .contact(new Contact()
                    .name("jIAn")
                    .email("eddiea.chen@gmail.com")
                    .url("https://github.com/jiansoft/stock_api")
                )
            );
    }
}
