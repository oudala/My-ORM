package com.myorm.models;

import java.util.List;

import com.myorm.annotations.Column;
import com.myorm.annotations.ManyToMany;
import com.myorm.annotations.Table;

@Table(name = "courses")
public class Course {
    @Column(name = "id", type = "INT", primaryKey = true)
    private int id;

    @Column(name = "title", type = "VARCHAR(255)")
    private String title;

    @ManyToMany(targetEntity = Student.class, joinTable = "student_courses", 
                joinColumn = "course_id", inverseJoinColumn = "student_id")
    private List<Student> students;

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}