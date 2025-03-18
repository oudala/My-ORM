package com.myorm.models;

import com.myorm.annotations.Column;
import com.myorm.annotations.ManyToOne;
import com.myorm.annotations.Table;

@Table(name = "orders")
public class Order {
    @Column(name = "id", type = "INT", primaryKey = true)
    private int id;

    @ManyToOne(targetEntity = User.class)
    @Column(name = "user_id", type = "INT")
    private User user;

    @Column(name = "total", type = "DECIMAL(10,2)")
    private double total;
}
