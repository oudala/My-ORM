package com.myorm.core;

import java.lang.reflect.Field;

import com.myorm.annotations.Column;
import com.myorm.annotations.Table;

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
}
