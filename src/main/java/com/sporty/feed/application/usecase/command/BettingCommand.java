package com.sporty.feed.application.usecase.command;

public sealed interface BettingCommand permits OddsChangeCommand, BetSettlementCommand {}
