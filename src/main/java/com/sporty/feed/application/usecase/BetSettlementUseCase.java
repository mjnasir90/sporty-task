package com.sporty.feed.application.usecase;

import com.sporty.feed.application.usecase.command.BetSettlementCommand;

/**
 * Input boundary for processing a bet-settlement event.
 */
public interface BetSettlementUseCase {

    void execute(BetSettlementCommand command);
}
