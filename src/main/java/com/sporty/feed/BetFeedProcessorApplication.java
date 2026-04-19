package com.sporty.feed;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Bet Feed Processor API",
                version = "1.0",
                description = "Ingest sports betting feeds from multiple providers."
        )
)
public class BetFeedProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(BetFeedProcessorApplication.class, args);
    }
}