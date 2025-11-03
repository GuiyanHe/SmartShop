package edu.tamu.csce634.smartshop.models;

import java.io.Serializable;

public class Ingredient implements Serializable {
    private String name;
    private String quantity;
    private int imageResId;

    public Ingredient(String name, String quantity, int imageResId) {
        this.name = name;
        this.quantity = quantity;
        this.imageResId = imageResId;
    }

    public String getName() { return name; }
    public String getQuantity() { return quantity; }
    public int getImageResId() { return imageResId; }
}