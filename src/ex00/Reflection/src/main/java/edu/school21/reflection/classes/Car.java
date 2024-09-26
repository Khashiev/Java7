package edu.school21.reflection.classes;

import java.util.StringJoiner;

public class Car {
    private String brand;
    private String model;
    private Double price;

    public Car() {
        this.brand = "Default brand";
        this.model = "Default model";
        this.price = 0D;
    }

    public Car(String brand, String model, Double price) {
        this.brand = brand;
        this.model = model;
        this.price = price;
    }

    public double increasePrice(Double value) {
        this.price += value;
        return price;
    }

    public void replace(String brand, String model) {
        this.brand = brand;
        this.model = model;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Car.class.getSimpleName() + "[", "]")
                .add("brand='" + brand + "'")
                .add("model='" + model + "'")
                .add("price=" + price)
                .toString();
    }
}
