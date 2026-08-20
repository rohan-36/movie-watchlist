package com.npst.watchlist.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI movieWatchlistOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Movie Watchlist API")
                        .version("v1")
                        .description(
                                "REST API for managing movies and their reviews."
                        ));
    }
}
