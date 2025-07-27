package com.tastytrade.shared.models.transaction;

public enum TransactionType {
    BUY_TO_OPEN,
    BUY_TO_CLOSE,
    SELL_TO_OPEN,
    SELL_TO_CLOSE,
    DIVIDEND,
    INTEREST,
    FEE,
    DEPOSIT,
    WITHDRAWAL,
    ADJUSTMENT,
    ASSIGNMENT,
    EXERCISE
}