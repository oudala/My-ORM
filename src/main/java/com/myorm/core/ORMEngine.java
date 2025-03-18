package com.myorm.core;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.myorm.annotations.Column;
import com.myorm.annotations.ManyToMany;
import com.myorm.annotations.OneToMany;
import com.myorm.annotations.Table;
import com.myorm.storage.DatabaseManager;

public class ORMEngine {
    public static void mapClass(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Table.class)) {
            Table table = clazz.getAnnotation(Table.class);
            System.out.println("Mapping table: " + table.name());

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    System.out.println("  - Column: " + column.name());
                }
            }
        }
    }
    private Object getId(Object entity) {
        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true); // Allow access to private fields
            try {
                if (field.isAnnotationPresent(Column.class) && field.getName().equalsIgnoreCase("id")) {
                    return field.get(entity); // Return ID value
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not access ID field", e);
            }
        }
        throw new RuntimeException("No ID field found in " + entity.getClass().getSimpleName());
    }
    private final Map<Class<?>, Map<Object, Object>> storage = new HashMap<>();

    public <T> void create(T entity) {
        Class<?> entityClass = entity.getClass();
        if (!entityClass.isAnnotationPresent(Table.class)) return;
    
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(entityClass.getAnnotation(Table.class).name()).append(" (");
        
        // Get all columns and their values
        List<Field> columns = new ArrayList<>();
        StringBuilder values = new StringBuilder();
        
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                columns.add(field);
                sql.append(column.name()).append(",");
                values.append("?,");
            }
        }
        
        // Remove last comma and complete the SQL statement
        sql.setLength(sql.length() - 1);
        values.setLength(values.length() - 1);
        sql.append(") VALUES (").append(values).append(")");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            // Set values for each column
            for (int i = 0; i < columns.size(); i++) {
                Field field = columns.get(i);
                field.setAccessible(true);
                stmt.setObject(i + 1, field.get(entity));
            }

            stmt.executeUpdate();
            System.out.println("✅ Data Inserted into " + entityClass.getAnnotation(Table.class).name());

        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    public <T> T findById(Class<T> clazz, Object id) {
        if (!clazz.isAnnotationPresent(Table.class)) return null;
    
        String sql = "SELECT * FROM " + clazz.getAnnotation(Table.class).name() + " WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
    
            if (rs.next()) {
                T instance = clazz.getDeclaredConstructor().newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Column.class)) {
                        field.setAccessible(true);
                        Column column = field.getAnnotation(Column.class);
                        field.set(instance, rs.getObject(column.name()));
                    }
                }
                return instance;
            }
    
        } catch (SQLException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Detect @OneToMany and fetch related objects
    private void loadOneToManyRelationships(Object entity) {
        Class<?> clazz = entity.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(OneToMany.class)) {
                field.setAccessible(true);
                OneToMany annotation = field.getAnnotation(OneToMany.class);
                Class<?> relatedClass = annotation.targetEntity();
                Map<Object, Object> relatedMap = storage.get(relatedClass);
                try {
                    field.set(entity, new ArrayList<>(relatedMap.values()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // Update - Modify existing object
    public <T> void update(T updatedEntity) {
        Class<?> entityClass = updatedEntity.getClass();
        Object id = getId(updatedEntity); // Extract ID dynamically
    
        Map<Object, Object> list = storage.get(entityClass);
        if (list != null) {
            for (Object key : list.keySet()) {
                if (key.equals(id)) {
                    list.put(key, updatedEntity);
                    return;
                }
            }
        }
        throw new RuntimeException("Entity with ID " + id + " not found for update!");
    }

    // Delete - Remove object by ID
    public <T> void delete(Class<T> clazz, Object id) {
        if (!clazz.isAnnotationPresent(Table.class)) return;
    
        String sql = "DELETE FROM " + clazz.getAnnotation(Table.class).name() + " WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
            System.out.println("✅ Data Deleted from " + clazz.getAnnotation(Table.class).name());
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public <T> T findByField(Class<T> clazz, String fieldName, Object value) {
        Map<Object, Object> entities = storage.get(clazz);
        if(entities != null ){
            for (Object entity : entities.values()){
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object fieldValue = field.get(entity);
                    if (fieldValue != null && fieldValue.equals(value)) {
                        return (T) entity;
                    }
                } catch (Exception  e) {
                    e.printStackTrace();
                }   
            }
        }
            return null;
        }

    public Map<Class<?>, Map<Object, Object>> getStorage() {
        return storage;
    }
    private void loadManyToManyRelationships(Object entity) {
        Class<?> clazz = entity.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(ManyToMany.class)) {
                field.setAccessible(true);
                ManyToMany annotation = field.getAnnotation(ManyToMany.class);
                Class<?> relatedClass = annotation.targetEntity();
                Map<Object, Object> relatedMap = storage.get(relatedClass);
                try {
                    field.set(entity, new ArrayList<>(relatedMap.values()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private Field findPrimaryKey(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) { 
                return field; // Assume first @Column is primary key
            }
        }
        return null; // No primary key found
    }
}
