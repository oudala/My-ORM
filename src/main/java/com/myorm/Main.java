package com.myorm;

import com.myorm.core.ORMEngine;
import com.myorm.models.User;

public class Main {
    public static void main(String[] args) {
        ORMEngine.mapClass(User.class);
    }
}
