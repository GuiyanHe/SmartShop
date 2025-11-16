package edu.tamu.csce634.smartshop.models;

public class ShoppingItem {
    public String ingredientId;
    public String name;
    public String unit;
    public double quantity;
    public String aisle;
    public double unitPrice;
    public String selectedSkuName;
    public String skuSpec;
    public String imageUrl;

    public String recipeNeededStr;
    public double recipeNeededValue;
    public String recipeNeededUnit;

    public String originalIngredientId;
    public double substitutionRatio = 1.0;
    public boolean isSubstituted = false;
    public String substituteDisplayName;

    public boolean isPicked = false;

}
