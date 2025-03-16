package com.myorm.models;

import com.myorm.annotations.Column;
import com.myorm.annotations.Table;

@Table(name = "users")
public class User {
    @Column(name = "id")
    private int id;

    @Column(name = "name")
    private String name;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
