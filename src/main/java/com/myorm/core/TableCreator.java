package com.myorm.core;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.myorm.annotations.Column;
import com.myorm.annotations.ManyToMany;
import com.myorm.annotations.ManyToOne;
import com.myorm.annotations.Table;
import com.myorm.storage.DatabaseManager;

public class TableCreator {

    private static final List<String> pendingJoinTables = new ArrayList<>();
    private static final Set<String> createdJoinTables = new HashSet<>();

    public static void generateSQLSchema(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("❌ Class " + clazz.getSimpleName() + " is not annotated with @Table");
        }

        String tableName = clazz.getAnnotation(Table.class).name();
        
        // Drop related join tables first
        dropJoinTables(clazz);
        
        // Then drop the main table
        dropTableIfExists(tableName);

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(tableName).append(" (");

        // Process columns
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                sql.append("`").append(column.name()).append("` ").append(column.type());
                if (column.primaryKey()) {
                    sql.append(" PRIMARY KEY AUTO_INCREMENT");
                }
                sql.append(", ");
            }
        }

        // Add foreign key constraints for ManyToOne
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ManyToOne.class)) {
                ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
                String referencedTable = manyToOne.targetEntity().getAnnotation(Table.class).name();
                
                // Get the corresponding Column annotation for this field
                for (Field f : clazz.getDeclaredFields()) {
                    if (f.isAnnotationPresent(Column.class)) {
                        Column column = f.getAnnotation(Column.class);
                        if (column.name().endsWith("_id")) {  // Foreign key column
                            sql.append("CONSTRAINT `fk_").append(tableName).append("_")
                               .append(column.name()).append("` ")
                               .append("FOREIGN KEY (`").append(column.name()).append("`) ")
                               .append("REFERENCES `").append(referencedTable).append("` (`id`) ")
                               .append("ON DELETE CASCADE ON UPDATE CASCADE")
                               .append(", ");
                            break;
                        }
                    }
                }
            }
        }

        // Store unique ManyToMany join table creation statements
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ManyToMany.class)) {
                ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
                String joinTableName = manyToMany.joinTable();
                
                if (!createdJoinTables.contains(joinTableName)) {
                    String joinTableSQL = generateJoinTableSQL(
                        tableName,
                        manyToMany.targetEntity().getAnnotation(Table.class).name(),
                        joinTableName,
                        manyToMany.joinColumn(),
                        manyToMany.inverseJoinColumn()
                    );
                    pendingJoinTables.add(joinTableSQL);
                    createdJoinTables.add(joinTableName);
                }
            }
        }

        sql.setLength(sql.length() - 2); // Remove last comma and space
        sql.append(");");

        executeSQL(sql.toString(), tableName);
    }

    private static void dropJoinTables(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ManyToMany.class)) {
                ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
                String joinTableName = manyToMany.joinTable();
                dropTableIfExists(joinTableName);
            }
        }
    }

    private static void dropTableIfExists(String tableName) {
        String sql = "DROP TABLE IF EXISTS `" + tableName + "`";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("✅ Dropped table if exists: " + tableName);
        } catch (SQLException e) {
            // Just log the error and continue
            System.out.println("ℹ️ Note: Could not drop table " + tableName);
        }
    }

    public static void createPendingJoinTables() {
        for (String joinTableSQL : pendingJoinTables) {
            executeSQL(joinTableSQL, "join table");
        }
        pendingJoinTables.clear();
        createdJoinTables.clear();
    }

    private static String generateJoinTableSQL(String table1, String table2, 
                                            String joinTableName, String joinColumn, 
                                            String inverseJoinColumn) {
        return String.format(
            "CREATE TABLE IF NOT EXISTS `%s` (" +
            "`%s` INT NOT NULL, " +
            "`%s` INT NOT NULL, " +
            "PRIMARY KEY (`%s`, `%s`), " +
            "CONSTRAINT `fk_%s_%s` FOREIGN KEY (`%s`) REFERENCES `%s` (`id`) " +
            "ON DELETE CASCADE ON UPDATE CASCADE, " +
            "CONSTRAINT `fk_%s_%s` FOREIGN KEY (`%s`) REFERENCES `%s` (`id`) " +
            "ON DELETE CASCADE ON UPDATE CASCADE" +
            ");",
            joinTableName,
            joinColumn, inverseJoinColumn,
            joinColumn, inverseJoinColumn,
            joinTableName, table1, joinColumn, table1,
            joinTableName, table2, inverseJoinColumn, table2
        );
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
