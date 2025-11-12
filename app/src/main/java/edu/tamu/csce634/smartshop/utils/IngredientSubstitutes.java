package edu.tamu.csce634.smartshop.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tamu.csce634.smartshop.R;

/**
 * 食材替代品映射表
 */
public class IngredientSubstitutes {

    /**
     * 替代品信息
     */
    public static class Substitute {
        public String name;              // 替代品名称
        public String ingredientId;      // 替代品ID（用于查找preset）
        public String note;              // 说明文字
        public String quantityAdjustment; // 数量调整说明（如 "1 → 2 Oz"）
        public int imageResId;           // 图片资源ID

        public Substitute(String name, String id, String note, String qtyAdj, int img) {
            this.name = name;
            this.ingredientId = id;
            this.note = note;
            this.quantityAdjustment = qtyAdj;
            this.imageResId = img;
        }
    }

    // === 纯素替代品 ===
    private static final Map<String, List<Substitute>> VEGAN_SUBSTITUTES = new HashMap<>();

    // === 素食替代品 ===
    private static final Map<String, List<Substitute>> VEGETARIAN_SUBSTITUTES = new HashMap<>();

    // === 无麸质替代品 ===
    private static final Map<String, List<Substitute>> GLUTEN_FREE_SUBSTITUTES = new HashMap<>();

    // === 过敏源替代品 ===
    private static final Map<String, Map<String, List<Substitute>>> ALLERGEN_FREE_SUBSTITUTES = new HashMap<>();

    static {
        initVeganSubstitutes();
        initVegetarianSubstitutes();
        initGlutenFreeSubstitutes();
        initAllergenFreeSubstitutes();
    }

    private static void initVeganSubstitutes() {
        // Egg的纯素替代品
        VEGAN_SUBSTITUTES.put("Egg", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Protein source", "1 egg → 3 Oz tofu", R.drawable.tofu),
                new Substitute("Chickpeas", "ing_chickpeas", "High protein", "1 egg → 2 Oz chickpeas", R.drawable.chickpea)
        ));

        // Steak的纯素替代品
        VEGAN_SUBSTITUTES.put("Steak", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Firm texture", "4 Oz steak → 5 Oz tofu", R.drawable.tofu),
                new Substitute("Chickpeas", "ing_chickpeas", "Protein-rich", "4 Oz steak → 6 Oz chickpeas", R.drawable.chickpea)
        ));

        // Salmon的纯素替代品
        VEGAN_SUBSTITUTES.put("Salmon", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Marinated option", "4 Oz salmon → 5 Oz tofu", R.drawable.tofu)
        ));

        // Queso Fresco的纯素替代品
        VEGAN_SUBSTITUTES.put("Queso Fresco", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Crumbled texture", "1 Oz cheese → 1.5 Oz tofu", R.drawable.tofu)
        ));
    }

    private static void initVegetarianSubstitutes() {
        // Steak的素食替代品
        VEGETARIAN_SUBSTITUTES.put("Steak", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Firm texture", "4 Oz steak → 5 Oz tofu", R.drawable.tofu),
                new Substitute("Chickpeas", "ing_chickpeas", "Protein-rich", "4 Oz steak → 6 Oz chickpeas", R.drawable.chickpea)
        ));

        // Salmon的素食替代品
        VEGETARIAN_SUBSTITUTES.put("Salmon", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Protein source", "4 Oz salmon → 5 Oz tofu", R.drawable.tofu)
        ));
    }

    private static void initGlutenFreeSubstitutes() {
        // Flour Tortilla的无麸质替代品
        GLUTEN_FREE_SUBSTITUTES.put("Flour Tortilla", Arrays.asList(
                new Substitute("Corn", "ing_corn", "Gluten-free wrap", "2 tortillas → 4 Oz corn", R.drawable.corn)
        ));

        // Brown Rice已经是无麸质的，但可以提供其他选择
        GLUTEN_FREE_SUBSTITUTES.put("Brown Rice", Arrays.asList(
                new Substitute("Quinoa", "ing_quinoa", "Higher protein", "1 Oz rice → 1 Oz quinoa", R.drawable.quinoa)
        ));
    }

    private static void initAllergenFreeSubstitutes() {
        // 鸡蛋过敏替代品
        Map<String, List<Substitute>> eggAllergySubs = new HashMap<>();
        eggAllergySubs.put("Egg", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Egg-free protein", "1 egg → 3 Oz tofu", R.drawable.tofu),
                new Substitute("Chickpeas", "ing_chickpeas", "Plant protein", "1 egg → 2 Oz chickpeas", R.drawable.chickpea)
        ));
        ALLERGEN_FREE_SUBSTITUTES.put("eggs", eggAllergySubs);

        // 乳制品过敏替代品
        Map<String, List<Substitute>> dairyAllergySubs = new HashMap<>();
        dairyAllergySubs.put("Queso Fresco", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Dairy-free alternative", "1 Oz cheese → 1.5 Oz tofu", R.drawable.tofu)
        ));
        ALLERGEN_FREE_SUBSTITUTES.put("dairy", dairyAllergySubs);

        // 大豆过敏替代品
        Map<String, List<Substitute>> soyAllergySubs = new HashMap<>();
        soyAllergySubs.put("Tofu", Arrays.asList(
                new Substitute("Chickpeas", "ing_chickpeas", "Soy-free protein", "2 Oz tofu → 2 Oz chickpeas", R.drawable.chickpea),
                new Substitute("Quinoa", "ing_quinoa", "Complete protein", "2 Oz tofu → 2 Oz quinoa", R.drawable.quinoa)
        ));
        ALLERGEN_FREE_SUBSTITUTES.put("soy", soyAllergySubs);
    }

    /**
     * 获取纯素替代品
     */
    public static List<Substitute> getVeganSubstitutes(String ingredientName) {
        return VEGAN_SUBSTITUTES.getOrDefault(ingredientName, new ArrayList<>());
    }

    /**
     * 获取素食替代品
     */
    public static List<Substitute> getVegetarianSubstitutes(String ingredientName) {
        List<Substitute> subs = VEGETARIAN_SUBSTITUTES.get(ingredientName);
        if (subs != null && !subs.isEmpty()) {
            return subs;
        }
        // 回退到纯素替代品
        return getVeganSubstitutes(ingredientName);
    }

    /**
     * 获取无麸质替代品
     */
    public static List<Substitute> getGlutenFreeSubstitutes(String ingredientName) {
        return GLUTEN_FREE_SUBSTITUTES.getOrDefault(ingredientName, new ArrayList<>());
    }

    /**
     * 获取指定过敏源的替代品
     */
    public static List<Substitute> getAllergenFreeSubstitutes(String ingredientName, String allergen) {
        Map<String, List<Substitute>> allergenSubs =
                ALLERGEN_FREE_SUBSTITUTES.get(allergen.toLowerCase());

        if (allergenSubs == null) {
            return new ArrayList<>();
        }

        return allergenSubs.getOrDefault(ingredientName, new ArrayList<>());
    }
}