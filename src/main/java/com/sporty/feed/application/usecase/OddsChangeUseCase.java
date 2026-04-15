package com.sporty.feed.application.usecase;

import com.sporty.feed.application.usecase.command.OddsChangeCommand;

/**
 * Input boundary for processing an odds-change event.
 */
public interface OddsChangeUseCase {

    void execute(OddsChangeCommand command);
}
