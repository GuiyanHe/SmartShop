package edu.tamu.csce634.smartshop.utils;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.models.ShoppingItem;
import edu.tamu.csce634.smartshop.models.world.Aisle;
import edu.tamu.csce634.smartshop.models.world.ItemZone;
import edu.tamu.csce634.smartshop.models.world.SupermarketLayout;

/**
 * The core engine for locating items within the supermarket layout.
 * It correctly reads category information from preset_items.json and finds the location
 * in the supermarket_layout.json world model.
 */
public class LocationEngine {

    private final SupermarketLayout supermarketLayout;
    private final Map<String, String> ingredientToCategoryMap; // Map<IngredientId, Category>

    public LocationEngine(Context context) {
        this.supermarketLayout = loadSupermarketLayout(context);
        this.ingredientToCategoryMap = createIngredientCategoryMap(context);
    }

    /**
     * The main public method. It populates coordinateX and coordinateY for a list of ShoppingItems.
     */
    public void calculateCoordinatesForList(List<ShoppingItem> shoppingList) {
        if (supermarketLayout == null || ingredientToCategoryMap.isEmpty()) {
            return; // Engine not initialized correctly
        }

        for (ShoppingItem item : shoppingList) {
            // 1. Get the category for this ingredientId
            String category = ingredientToCategoryMap.get(item.ingredientId);
            if (category == null) {
                continue; // Cannot find category for this item
            }

            // 2. Find the location for this category in our world model
            PointF location = findLocationForCategory(category);
            if (location != null) {
                item.coordinateX = location.x;
                item.coordinateY = location.y;
            }
        }
    }

    /**
     * Finds the absolute center coordinate for a given category by searching the world model.
     */
    private PointF findLocationForCategory(String category) {
        if (supermarketLayout == null || supermarketLayout.aisles == null) return null;

        for (Aisle aisle : supermarketLayout.aisles) {
            if (aisle.itemZones == null) continue;

            for (ItemZone zone : aisle.itemZones) {
                if (category.equals(zone.category)) {
                    // Category found! Calculate its center point within the aisle.
                    float zoneCenterInAisle = (zone.start + zone.end) / 2.0f;

                    // This calculation assumes a vertically oriented aisle for simplicity.
                    float absoluteX = aisle.x + (aisle.width / 2.0f);
                    float absoluteY = aisle.y + (aisle.height * zoneCenterInAisle);

                    return new PointF(absoluteX, absoluteY);
                }
            }
        }
        return null; // Category not found in the supermarket_layout.json
    }

    // --- Helper methods for loading data ---

    private SupermarketLayout loadSupermarketLayout(Context context) {
        try (InputStream is = context.getResources().openRawResource(R.raw.supermarket_layout)) {
            return new Gson().fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), SupermarketLayout.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * THIS IS THE CORE LOGIC: Read from preset_items.json to create the
     * IngredientID -> Category mapping.
     */
    private Map<String, String> createIngredientCategoryMap(Context context) {
        Map<String, String> map = new HashMap<>();
        try (InputStream is = context.getResources().openRawResource(R.raw.preset_items)) {
            // preset_items.json is a JSON object with a key "items" which is a list.
            // We need a wrapper class to parse this structure.
            PresetItemsWrapper wrapper = new Gson().fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), PresetItemsWrapper.class);

            if (wrapper != null && wrapper.items != null) {
                for (ShoppingItem presetItem : wrapper.items) {
                    if (presetItem.ingredientId != null && presetItem.category != null) {
                        map.put(presetItem.ingredientId, presetItem.category);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Inner wrapper class to help Gson parse the root object of preset_items.json.
     */
    private static class PresetItemsWrapper {
        List<ShoppingItem> items;
    }

    /**
     * A simple inner PointF class to hold coordinate data.
     */
    private static class PointF {
        float x;
        float y;

        PointF(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
