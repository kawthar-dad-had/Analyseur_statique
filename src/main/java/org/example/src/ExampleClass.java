package org.example.src;

public class ExampleClass extends SuperClass {
    private int number;
    protected String name;
    public static final double PI = 3.14;

    // Constructeur
    public ExampleClass(int number, String name) {
        this.number = number;
        this.name = name;
    }

    // Méthode publique
    public void printDetails() {
        System.out.println("Number: " + number);
        System.out.println("Name: " + name);
        anotherMethod();
    }

    // Méthode privée
    private void anotherMethod() {
        System.out.println("This is a private method.");
    }


}

class SuperClass {
    public void greet() {
        System.out.println("Hello from SuperClass!");
    }
}


