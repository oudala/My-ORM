package com.myorm.core;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.myorm.annotations.Column;
import com.myorm.annotations.ManyToOne;
import com.myorm.annotations.Table;
import com.myorm.storage.DatabaseManager;

public class TableCreator {

    private static final List<String> pendingForeignKeys = new ArrayList<>();

    public static void generateSQLSchema(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("❌ Class " + clazz.getSimpleName() + " is not annotated with @Table");
        }

        // Drop existing table if exists
        dropTableIfExists(clazz);

        StringBuilder sql = new StringBuilder();
        String tableName = clazz.getAnnotation(Table.class).name();
        sql.append("CREATE TABLE ").append(tableName).append(" (");

        // Process columns
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                sql.append(column.name()).append(" ").append(column.type());
                if (column.primaryKey()) {
                    sql.append(" PRIMARY KEY AUTO_INCREMENT");
                }
                sql.append(", ");
            }
        }

        // Add foreign key constraints
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ManyToOne.class) && field.isAnnotationPresent(Column.class)) {
                ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
                Column column = field.getAnnotation(Column.class);
                String referencedTable = manyToOne.targetEntity().getAnnotation(Table.class).name();
                
                // Add foreign key constraint
                sql.append("CONSTRAINT `fk_").append(tableName).append("_")
                   .append(column.name()).append("` ")
                   .append("FOREIGN KEY (`").append(column.name()).append("`) ")
                   .append("REFERENCES `").append(referencedTable).append("` (`id`) ")
                   .append("ON DELETE CASCADE ON UPDATE CASCADE")
                   .append(", ");
            }
        }

        // Remove last comma and space
        sql.setLength(sql.length() - 2);
        sql.append(");");

        executeSQL(sql.toString(), tableName);
    }

    private static void dropTableIfExists(Class<?> clazz) {
        String tableName = clazz.getAnnotation(Table.class).name();
        String sql = "DROP TABLE IF EXISTS " + tableName;
        
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("✅ Dropped existing table: " + tableName);
        } catch (SQLException e) {
            System.err.println("❌ Error dropping table " + tableName + ": " + e.getMessage());
        }
    }

    private static void executeSQL(String sql, String tableName) {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("✅ Table Created with relationships: " + tableName);
            System.out.println("SQL: " + sql);  // Print SQL for debugging
        } catch (SQLException e) {
            System.err.println("❌ Error creating table " + tableName + ": " + e.getMessage());
            System.err.println("SQL: " + sql);  // Print SQL on error
        }
    }
}
