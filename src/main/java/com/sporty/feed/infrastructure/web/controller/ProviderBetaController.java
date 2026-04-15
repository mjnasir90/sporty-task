package com.sporty.feed.infrastructure.web.controller;

import com.sporty.feed.application.usecase.BetSettlementUseCase;
import com.sporty.feed.application.usecase.OddsChangeUseCase;
import com.sporty.feed.application.usecase.command.BetSettlementCommand;
import com.sporty.feed.application.usecase.command.OddsChangeCommand;
import com.sporty.feed.infrastructure.web.dto.beta.BetaFeedRequest;
import com.sporty.feed.infrastructure.web.mapper.BetaFeedMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/provider-beta")
@RequiredArgsConstructor
public class ProviderBetaController {

    private final BetaFeedMapper mapper;
    private final OddsChangeUseCase oddsChangeUseCase;
    private final BetSettlementUseCase betSettlementUseCase;

    @PostMapping("/feed")
    public ResponseEntity<Void> handleFeed(@RequestBody @Valid BetaFeedRequest request) {
        switch (mapper.toCommand(request)) {
            case OddsChangeCommand cmd    -> oddsChangeUseCase.execute(cmd);
            case BetSettlementCommand cmd -> betSettlementUseCase.execute(cmd);
        }
        return ResponseEntity.accepted().build();
    }
}
