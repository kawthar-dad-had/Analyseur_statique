package org.example.src;

public class Student extends Person {
    private String studentId;
    private double gpa;

    public Student(String name, int age, String studentId, double gpa) {
        super(name, age);
        this.studentId = studentId;
        this.gpa = gpa;
    }

    public void study() {
        System.out.println(getName() + " is studying.");
    }

    public void displayStudentInfo() {
        displayInfo();
        System.out.println("Student ID: " + studentId + ", GPA: " + gpa);
    }
}
