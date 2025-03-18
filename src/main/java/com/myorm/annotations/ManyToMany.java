package com.myorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ManyToMany {
    Class<?> targetEntity();
    String joinTable();  // The table name for the join relationship
    String joinColumn(); // The column that references this entity
    String inverseJoinColumn(); // The column that references the other entity
}