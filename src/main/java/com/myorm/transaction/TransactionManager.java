package com.myorm.transaction;

import java.sql.Connection;
import java.sql.SQLException;

import com.myorm.storage.DatabaseManager;

public class TransactionManager {
    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> transactionActive = new ThreadLocal<>();

    public static void beginTransaction() throws SQLException {
        if (isTransactionActive()) {
            throw new SQLException("Transaction already active");
        }

        Connection conn = DatabaseManager.getConnection();
        conn.setAutoCommit(false);
        connectionHolder.set(conn);
        transactionActive.set(true);
        System.out.println("🔄 Transaction started");
    }

    public static void commit() throws SQLException {
        Connection conn = getActiveConnection();
        try {
            conn.commit();
            System.out.println("✅ Transaction committed");
        } finally {
            cleanup();
        }
    }

    public static void rollback() {
        try {
            Connection conn = getActiveConnection();
            conn.rollback();
            System.out.println("⏮️ Transaction rolled back");
        } catch (SQLException e) {
            System.err.println("❌ Error during rollback: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    public static Connection getCurrentConnection() throws SQLException {
        Connection conn = connectionHolder.get();
        if (conn == null) {
            // If no transaction is active, return a new auto-commit connection
            return DatabaseManager.getConnection();
        }
        return conn;
    }

    private static Connection getActiveConnection() throws SQLException {
        Connection conn = connectionHolder.get();
        if (conn == null || !isTransactionActive()) {
            throw new SQLException("No active transaction");
        }
        return conn;
    }

    private static boolean isTransactionActive() {
        return Boolean.TRUE.equals(transactionActive.get());
    }

    private static void cleanup() {
        try {
            Connection conn = connectionHolder.get();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException e) {
            System.err.println("❌ Error during cleanup: " + e.getMessage());
        } finally {
            connectionHolder.remove();
            transactionActive.remove();
        }
    }
} 