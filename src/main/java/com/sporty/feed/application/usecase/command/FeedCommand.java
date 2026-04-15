package com.sporty.feed.application.usecase.command;

public sealed interface FeedCommand permits OddsChangeCommand, BetSettlementCommand {}
