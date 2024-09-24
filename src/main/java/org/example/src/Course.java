package org.example.src;

import java.util.ArrayList;
import java.util.List;

public class Course {
    private String courseName;
    private List<Student> students;

    public Course(String courseName) {
        this.courseName = courseName;
        this.students = new ArrayList<>();
    }

    public void enrollStudent(Student student) {
        students.add(student);
    }

    public void listStudents() {
        System.out.println("Course: " + courseName);
        for (Student student : students) {
            student.displayInfo();
        }
    }
}
