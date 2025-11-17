package edu.tamu.csce634.smartshop.models;

public class ProfileData {
    private String name;
    private String email;
    private int height; // in cm
    private int weight; // in kg
    private String goal; // e.g., "Maintain", "Fat Loss"
    private String store;

    // Updated Goals
    private int calorieGoal;
    private int proteinGoal;
    private int fatGoal;
    private int waterGoal; // in cups

    // Shopping Preferences
    private boolean isVegetarian;
    private boolean isVegan;
    private boolean organicOnly;
    private boolean glutenFree;
    // allergic info（JSON,e.g, ["nuts", "dairy"]）
    private String allergies;
    // brand preference（JSON, e.g., ["GreenValley", "NatureBest"]）
    private String favoriteBrands;
    // budget
    private float maxItemPrice;
    private boolean preferDeals;
    // package size preference（"small", "regular", "large"）
    private String packageSizePref;

    public ProfileData(String name, String email, int height, int weight, String goal, String store,
                       int calorieGoal, int proteinGoal, int fatGoal, int waterGoal) {
        this.name = name;
        this.email = email;
        this.height = height;
        this.weight = weight;
        this.goal = goal;
        this.store = store;
        this.calorieGoal = calorieGoal;
        this.proteinGoal = proteinGoal;
        this.fatGoal = fatGoal;
        this.waterGoal = waterGoal;
    }

    // Constructor including Preferences
    public ProfileData(String name, String email, int height, int weight, String goal, String store,
                       int calorieGoal, int proteinGoal, int fatGoal, int waterGoal,
                       boolean isVegetarian, boolean isVegan, boolean organicOnly, boolean glutenFree,
                       String allergies, String favoriteBrands, float maxItemPrice,
                       boolean preferDeals, String packageSizePref) {
        this(name, email, height, weight, goal, store, calorieGoal, proteinGoal, fatGoal, waterGoal);

        this.isVegetarian = isVegetarian;
        this.isVegan = isVegan;
        this.organicOnly = organicOnly;
        this.glutenFree = glutenFree;
        this.allergies = allergies;
        this.favoriteBrands = favoriteBrands;
        this.maxItemPrice = maxItemPrice;
        this.preferDeals = preferDeals;
        this.packageSizePref = packageSizePref;
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getHeight() { return height; }
    public int getWeight() { return weight; }
    public String getGoal() { return goal; }
    public String getStore() { return store; }
    public int getCalorieGoal() { return calorieGoal; }
    public int getProteinGoal() { return proteinGoal; }
    public int getFatGoal() { return fatGoal; }
    public int getWaterGoal() { return waterGoal; }

    // Preference Getters & Setters

    public boolean isVegetarian() { return isVegetarian; }
    public void setVegetarian(boolean vegetarian) { isVegetarian = vegetarian; }

    public boolean isVegan() { return isVegan; }
    public void setVegan(boolean vegan) { isVegan = vegan; }

    public boolean isOrganicOnly() { return organicOnly; }
    public void setOrganicOnly(boolean organicOnly) { this.organicOnly = organicOnly; }

    public boolean isGlutenFree() { return glutenFree; }
    public void setGlutenFree(boolean glutenFree) { this.glutenFree = glutenFree; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getFavoriteBrands() { return favoriteBrands; }
    public void setFavoriteBrands(String favoriteBrands) { this.favoriteBrands = favoriteBrands; }

    public float getMaxItemPrice() { return maxItemPrice; }
    public void setMaxItemPrice(float maxItemPrice) { this.maxItemPrice = maxItemPrice; }

    public boolean isPreferDeals() { return preferDeals; }
    public void setPreferDeals(boolean preferDeals) { this.preferDeals = preferDeals; }

    public String getPackageSizePref() { return packageSizePref; }
    public void setPackageSizePref(String packageSizePref) { this.packageSizePref = packageSizePref; }

    // parse JSON
    public java.util.List<String> getAllergiesList() {
        try {
            org.json.JSONArray arr = new org.json.JSONArray(allergies != null ? allergies : "[]");
            java.util.List<String> list = new java.util.ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.getString(i));
            }
            return list;
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    public java.util.List<String> getFavoriteBrandsList() {
        try {
            org.json.JSONArray arr = new org.json.JSONArray(favoriteBrands != null ? favoriteBrands : "[]");
            java.util.List<String> list = new java.util.ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.getString(i));
            }
            return list;
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }

    // Default values
    public static ProfileData createDefault() {
        return new ProfileData(
                "Guest User",
                "guest@smartshop.com",
                170,
                70,
                "Maintain Weight",
                "SmartShop Market",
                2000,
                50,
                65,
                8,
                // preference
                false,        // isVegetarian
                false,        // isVegan
                false,        // organicOnly
                false,        // glutenFree
                "[]",         // allergies
                "[]",         // favoriteBrands
                20.0f,      // maxItemPrice
                false,        // preferDeals
                "regular"     // packageSizePref
        );
    }
}
