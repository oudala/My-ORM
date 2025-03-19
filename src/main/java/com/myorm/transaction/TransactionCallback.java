package com.myorm.transaction;

@FunctionalInterface
public interface TransactionCallback {
    void execute() throws Exception;
} 