package com.myorm.models;

import com.myorm.annotations.Column;
import com.myorm.annotations.ManyToOne;
import com.myorm.annotations.Table;

@Table(name = "orders")
public class Order {
    @Column(name = "id", type = "INT", primaryKey = true)
    private int id;

    @Column(name = "user_id", type = "INT")
    private int userId;

    @Column(name = "total", type = "DECIMAL(10,2)")
    private double total;

    @ManyToOne(targetEntity = User.class)
    private User user;

    public void setUser(User user) {
        this.user = user;
        this.userId = user.getId();
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getTotal() {
        return total;
    }
}
