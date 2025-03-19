package com.myorm.core;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.myorm.annotations.Column;
import com.myorm.annotations.Table;
import com.myorm.storage.DatabaseManager;
import com.myorm.transaction.TransactionCallback;
import com.myorm.transaction.TransactionManager;

public class CRUDManager {
    
    public static <T> void insert(T entity) {
        Class<?> clazz = entity.getClass();
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Class is not a table entity");
        }

        String tableName = clazz.getAnnotation(Table.class).name();
        StringBuilder sql = new StringBuilder("INSERT INTO `").append(tableName).append("` (");
        List<Field> fields = new ArrayList<>();

        // Build column names
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (!column.primaryKey()) { // Skip auto-increment primary key
                    sql.append("`").append(column.name()).append("`, ");
                    fields.add(field);
                }
            }
        }
        sql.setLength(sql.length() - 2); // Remove last comma and space
        
        // Build values placeholders
        sql.append(") VALUES (").append("?, ".repeat(fields.size()));
        sql.setLength(sql.length() - 2); // Remove last comma and space
        sql.append(")");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = TransactionManager.getCurrentConnection()
                     .prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            
            // Set values
            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                field.setAccessible(true);
                Object value = field.get(entity);
                
                // Handle null values
                if (value == null) {
                    stmt.setNull(i + 1, java.sql.Types.NULL);
                } else {
                    stmt.setObject(i + 1, value);
                }
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating entity failed, no rows affected.");
            }

            // Get generated ID and set it to the entity
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    for (Field field : clazz.getDeclaredFields()) {
                        if (field.isAnnotationPresent(Column.class) && 
                            field.getAnnotation(Column.class).primaryKey()) {
                            field.setAccessible(true);
                            field.set(entity, generatedKeys.getInt(1));
                            break;
                        }
                    }
                }
            }

            System.out.println("✅ Entity inserted successfully with ID: " + getEntityId(entity));

        } catch (SQLException | IllegalAccessException e) {
            System.err.println("❌ Error inserting entity: " + e.getMessage());
        }
    }

    private static int getEntityId(Object entity) throws IllegalAccessException {
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class) && 
                field.getAnnotation(Column.class).primaryKey()) {
                field.setAccessible(true);
                return (int) field.get(entity);
            }
        }
        return -1;
    }

    public static <T> T findById(Class<T> clazz, int id) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Class is not a table entity");
        }

        String tableName = clazz.getAnnotation(Table.class).name();
        String sql = "SELECT * FROM `" + tableName + "` WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEntity(rs, clazz);
            }

        } catch (SQLException | ReflectiveOperationException e) {
            System.err.println("❌ Error finding entity by ID: " + e.getMessage());
        }

        return null;
    }

    public static <T> List<T> findAll(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Class is not a table entity");
        }

        List<T> results = new ArrayList<>();
        String tableName = clazz.getAnnotation(Table.class).name();
        String sql = "SELECT * FROM `" + tableName + "`";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                T entity = mapResultSetToEntity(rs, clazz);
                results.add(entity);
            }

        } catch (SQLException | ReflectiveOperationException e) {
            System.err.println("❌ Error finding all entities: " + e.getMessage());
        }

        return results;
    }

    private static <T> T mapResultSetToEntity(ResultSet rs, Class<T> clazz) 
            throws SQLException, ReflectiveOperationException {
        T entity = clazz.getDeclaredConstructor().newInstance();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                field.setAccessible(true);
                
                Object value = rs.getObject(column.name());
                if (value != null) {
                    // Handle type conversion if needed
                    if (field.getType() == int.class || field.getType() == Integer.class) {
                        field.set(entity, rs.getInt(column.name()));
                    } else if (field.getType() == long.class || field.getType() == Long.class) {
                        field.set(entity, rs.getLong(column.name()));
                    } else if (field.getType() == double.class || field.getType() == Double.class) {
                        field.set(entity, rs.getDouble(column.name()));
                    } else if (field.getType() == boolean.class || field.getType() == Boolean.class) {
                        field.set(entity, rs.getBoolean(column.name()));
                    } else {
                        field.set(entity, value);
                    }
                }
            }
        }

        return entity;
    }

    public static <T> void update(T entity) {
        Class<?> clazz = entity.getClass();
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Class is not a table entity");
        }

        String tableName = clazz.getAnnotation(Table.class).name();
        StringBuilder sql = new StringBuilder("UPDATE `").append(tableName).append("` SET ");
        List<Field> fields = new ArrayList<>();
        Field idField = null;

        // Build SET clause and collect fields
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (column.primaryKey()) {
                    idField = field;
                    continue;
                }
                sql.append("`").append(column.name()).append("` = ?, ");
                fields.add(field);
            }
        }
        sql.setLength(sql.length() - 2); // Remove last comma and space
        sql.append(" WHERE id = ?");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            // Set values for UPDATE
            int paramIndex = 1;
            for (Field field : fields) {
                field.setAccessible(true);
                stmt.setObject(paramIndex++, field.get(entity));
            }

            // Set ID for WHERE clause
            idField.setAccessible(true);
            stmt.setObject(paramIndex, idField.get(entity));

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Entity updated successfully");
            } else {
                System.out.println("❌ No entity found to update");
            }

        } catch (SQLException | IllegalAccessException e) {
            System.err.println("❌ Error updating entity: " + e.getMessage());
        }
    }

    public static <T> void delete(Class<T> clazz, int id) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Class is not a table entity");
        }

        String tableName = clazz.getAnnotation(Table.class).name();
        String sql = "DELETE FROM `" + tableName + "` WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Entity deleted successfully");
            } else {
                System.out.println("❌ No entity found to delete");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error deleting entity: " + e.getMessage());
        }
    }

    public static void executeInTransaction(TransactionCallback callback) {
        try {
            TransactionManager.beginTransaction();
            callback.execute();
            TransactionManager.commit();
        } catch (Exception e) {
            TransactionManager.rollback();
            throw new RuntimeException("Transaction failed: " + e.getMessage(), e);
        }
    }
} 