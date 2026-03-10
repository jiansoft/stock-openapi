package com.jiansoft.stock_api.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

class OpenApiConfigTest {

    @Test
    void stockApiOpenApiUsesRelativeServerUrl() {
        OpenAPI openAPI = new OpenApiConfig().stockApiOpenAPI();

        assertThat(openAPI.getServers())
            .singleElement()
            .extracting("url")
            .isEqualTo("/");
    }
}
