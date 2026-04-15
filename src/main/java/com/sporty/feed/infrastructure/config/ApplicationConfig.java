package com.sporty.feed.infrastructure.config;

import com.sporty.feed.application.gateway.DomainEventPublisher;
import com.sporty.feed.application.service.BetSettlementInteractor;
import com.sporty.feed.application.service.OddsChangeInteractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires application-layer interactors into the Spring context.
 * The interactors themselves have zero Spring imports — infrastructure owns the wiring.
 */
@Configuration
public class ApplicationConfig {

    @Bean
    public OddsChangeInteractor oddsChangeInteractor(DomainEventPublisher domainEventPublisher) {
        return new OddsChangeInteractor(domainEventPublisher);
    }

    @Bean
    public BetSettlementInteractor betSettlementInteractor(DomainEventPublisher domainEventPublisher) {
        return new BetSettlementInteractor(domainEventPublisher);
    }
}
