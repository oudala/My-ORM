package com.myorm.models;

import java.util.List;

import com.myorm.annotations.Column;
import com.myorm.annotations.ManyToMany;
import com.myorm.annotations.Table;

@Table(name = "students")
public class Student {
    @Column(name = "id", type = "INT PRIMARY KEY")
    private int id;

    @Column(name = "name", type = "VARCHAR(255)")
    private String name;

    @ManyToMany(targetEntity = Course.class, joinTable = "student_courses", 
                joinColumn = "student_id", inverseJoinColumn = "course_id")
    private List<Course> courses;
}
