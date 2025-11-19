package edu.tamu.csce634.smartshop.models;

import com.google.gson.annotations.SerializedName;

/**
 * Data class representing a specific product option for an ingredient.
 * This class is designed to be used with Gson for parsing options.json.
 * It maps the fields from the JSON object to Java fields.
 */
public class ProductOption {

    @SerializedName("name")
    public String name;

    @SerializedName("price")
    public double price;

    @SerializedName("spec")
    public String spec;

    @SerializedName("aisle")
    public String aisle;

    @SerializedName("category")
    public String category; // This field is crucial for LocationEngine

    @SerializedName("image_url")
    public String imageUrl;

    // The following fields might also be in your JSON, based on ProductOptionsBottomSheet
    @SerializedName("displayName")
    public String displayName;

    @SerializedName("size")
    public String size;

    @SerializedName("isOrganic")
    public boolean isOrganic;

    @SerializedName("unitPrice")
    public double unitPrice;
}
