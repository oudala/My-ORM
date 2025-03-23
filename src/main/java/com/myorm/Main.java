package com.myorm;

import java.util.Arrays;
import java.util.List;

import com.myorm.core.CRUDManager;
import com.myorm.core.TableCreator;
import com.myorm.models.Course;
import com.myorm.models.Order;
import com.myorm.models.Student;
import com.myorm.models.User;

public class Main {
    public static void main(String[] args) {
        System.out.println("\n🚀 Starting ORM Tests...\n");

        // 1. Create Tables
        System.out.println("📊 Creating Tables...");
        TableCreator.generateSQLSchema(User.class);
        TableCreator.generateSQLSchema(Order.class);
        TableCreator.generateSQLSchema(Student.class);
        TableCreator.generateSQLSchema(Course.class);
        TableCreator.createPendingJoinTables();

        // 2. Test User-Order (OneToMany) Relationship
        System.out.println("\n🧪 Testing User-Order Relationship...");
        User user = new User();
        user.setName("John Doe");
        CRUDManager.insert(user);
        System.out.println("Created User: " + user.getName() + " (ID: " + user.getId() + ")");

        Order order1 = new Order();
        order1.setUser(user);
        order1.setTotal(99.99);
        CRUDManager.insert(order1);
        System.out.println("Created Order: $" + order1.getTotal() + " for user: " + user.getName());

        // 3. Test Student-Course (ManyToMany) Relationship
        System.out.println("\n🧪 Testing Student-Course Relationship...");
        Student student1 = new Student();
        student1.setName("Alice Smith");
        CRUDManager.insert(student1);
        System.out.println("Created Student: " + student1.getName());

        Course course1 = new Course();
        course1.setTitle("Mathematics");
        CRUDManager.insert(course1);
        System.out.println("Created Course: " + course1.getTitle());

        Course course2 = new Course();
        course2.setTitle("Physics");
        CRUDManager.insert(course2);
        System.out.println("Created Course: " + course2.getTitle());

        // Add courses to student
        student1.setCourses(Arrays.asList(course1, course2));
        CRUDManager.update(student1);

        // 4. Test Find Operations
        System.out.println("\n🔍 Testing Find Operations...");
        
        Student foundStudent = CRUDManager.findById(Student.class, student1.getId());
        System.out.println("Found Student by ID: " + (foundStudent != null ? foundStudent.getName() : "Not found"));

        List<Student> allStudents = CRUDManager.findAll(Student.class);
        System.out.println("\nAll Students:");
        if (allStudents != null && !allStudents.isEmpty()) {
            for (Student s : allStudents) {
                System.out.println("- " + s.getName());
            }
        } else {
            System.out.println("No students found");
        }

        // 5. Test Delete Operation
        System.out.println("\n❌ Testing Delete Operation...");
        CRUDManager.delete(Student.class, student1.getId());
        System.out.println("Deleted Student: " + student1.getName());

        allStudents = CRUDManager.findAll(Student.class);
        System.out.println("Students after delete: " + (allStudents != null ? allStudents.size() : "null"));

        System.out.println("\n✅ All tests completed!\n");

        // Remove the first transaction example and keep only the test cases
        System.out.println("\n🧪 Testing Transactions...");

        // Test 1: Successful Transaction
        try {
            CRUDManager.executeInTransaction(() -> {
                Student transStudent = new Student();
                transStudent.setName("Transaction Test Student");
                CRUDManager.insert(transStudent);
                System.out.println("Created student in transaction: " + transStudent.getName());

                Course transCourse = new Course();
                transCourse.setTitle("Transaction Test Course");
                CRUDManager.insert(transCourse);
                System.out.println("Created course in transaction: " + transCourse.getTitle());
            });
            System.out.println("✅ Successful transaction completed\n");
        } catch (Exception e) {
            System.err.println("❌ Transaction failed: " + e.getMessage());
        }

        // Test 2: Failed Transaction (will rollback)
        try {
            CRUDManager.executeInTransaction(() -> {
                Student transStudent = new Student();
                transStudent.setName("Will be rolled back");
                CRUDManager.insert(transStudent);
                System.out.println("Created student that will be rolled back: " + transStudent.getName());

                // Simulate an error
                if (true) {
                    throw new RuntimeException("Simulated error to test rollback");
                }

                // This code won't execute due to the error
                Course transCourse = new Course();
               transCourse.setTitle("Won't be created");
                CRUDManager.insert(transCourse);
            });
        } catch (Exception e) {
            System.err.println("❌ Expected transaction failure: " + e.getMessage());
        }

        // Verify the results
        System.out.println("\n🔍 Verifying Transaction Results:");
        List<Student> students = CRUDManager.findAll(Student.class);
        System.out.println("Students in database: " + (students != null ? students.size() : "null"));
        if (students != null) {
            students.forEach(s -> System.out.println("- " + s.getName()));
        }

        // Add this after your existing tests
        System.out.println("\n🧪 Testing Cache...");

        // Test cache miss then hit
        Student student = new Student();
        student.setName("Cache Test Student");
        CRUDManager.insert(student);
        System.out.println("Created student with ID: " + student.getId());

        // First find - should be cache miss
        Student found1 = CRUDManager.findById(Student.class, student.getId());
        System.out.println("First find: " + (found1 != null ? found1.getName() : "Not found"));

        // Second find - should be cache hit
        Student found2 = CRUDManager.findById(Student.class, student.getId());
        System.out.println("Second find: " + (found2 != null ? found2.getName() : "Not found"));

        // Update student
        student.setName("Updated Name");
        CRUDManager.update(student);

        // Find after update - should get updated value
        Student found3 = CRUDManager.findById(Student.class, student.getId());
        System.out.println("After update: " + (found3 != null ? found3.getName() : "Not found"));

        // Delete student
        CRUDManager.delete(Student.class, student.getId());

        // Try to find deleted student - should be null
        Student found4 = CRUDManager.findById(Student.class, student.getId());
        System.out.println("After delete: " + (found4 == null ? "null" : found4.getName()));

        // Print cache statistics
        System.out.println("\n📊 Cache Statistics:");
        System.out.println(CRUDManager.getCacheStats());
    }
}