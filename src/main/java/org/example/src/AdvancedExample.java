package org.example.src;

public class AdvancedExample extends BaseClass implements MyInterface {
    private int age;
    protected String firstName;
    public static final boolean isActive = true;

    public AdvancedExample(int age, String firstName) {
        this.age = age;
        this.firstName = firstName;
    }

    @Override
    public void interfaceMethod() {
        System.out.println("Implementing interface method.");
    }

    public void showDetails() {
        System.out.println("Age: " + age);
        System.out.println("First Name: " + firstName);
        calculateAgeInMonths();
    }

    private int calculateAgeInMonths() {
        return age * 12;
    }
}

class BaseClass {
    public void greet() {
        System.out.println("Hello from BaseClass!");
    }
}

interface MyInterface {
    void interfaceMethod();
}
