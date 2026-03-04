package com.jiansoft.stock_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 相關設定。
 *
 * <p>目前主要用於開放 API 的 CORS 規則，讓前端或其他服務可跨網域呼叫。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 設定全域 CORS 規則。
     *
     * @param registry CORS 設定登錄器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedMethods("*")
            .allowedHeaders("*");
    }
}
