package com.myorm;

import com.myorm.core.TableCreator;
import com.myorm.models.Order;
import com.myorm.models.User;

public class Main {
    public static void main(String[] args) {
        // Generate SQL Schema and Store in MySQL - Create parent tables first
        TableCreator.generateSQLSchema(User.class);  // Parent table with primary key
        TableCreator.generateSQLSchema(Order.class); // Child table with foreign key
    }
}
