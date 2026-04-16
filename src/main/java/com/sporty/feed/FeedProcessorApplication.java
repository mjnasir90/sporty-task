package com.sporty.feed;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Feed Processor API",
                version = "1.0",
                description = "Normalises sports betting feeds from multiple providers into a unified internal format."
        )
)
public class FeedProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeedProcessorApplication.class, args);
    }
}