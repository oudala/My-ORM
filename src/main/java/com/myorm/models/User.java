package com.myorm.models;

import java.util.List;

import com.myorm.annotations.Column;
import com.myorm.annotations.OneToMany;
import com.myorm.annotations.Table;

@Table(name = "users")
public class User {
    @Column(name = "id", type = "INT", primaryKey = true)
    private int id;

    @Column(name = "name", type = "VARCHAR(255)")
    private String name;

    @OneToMany(targetEntity = Order.class, mappedBy = "user")
    private List<Order> orders;
}
