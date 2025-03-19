package com.myorm.models;

import java.util.List;

import com.myorm.annotations.Column;
import com.myorm.annotations.ManyToMany;
import com.myorm.annotations.Table;

@Table(name = "students")
public class Student {
    @Column(name = "id", type = "INT", primaryKey = true)
    private int id;

    @Column(name = "name", type = "VARCHAR(255)")
    private String name;

    @ManyToMany(targetEntity = Course.class, joinTable = "student_courses", 
                joinColumn = "student_id", inverseJoinColumn = "course_id")
    private List<Course> courses;

    public Student() {}

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<Course> getCourses() {
        return courses;
    }
}
