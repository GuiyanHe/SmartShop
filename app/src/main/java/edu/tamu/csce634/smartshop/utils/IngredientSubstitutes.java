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
        // === Egg的纯素替代品 ===
        VEGAN_SUBSTITUTES.put("Egg", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Scrambled option", "1 egg → 3 Oz tofu", R.drawable.tofu),
                new Substitute("Chickpeas", "ing_chickpeas", "Baking substitute", "1 egg → 2 Oz chickpeas", R.drawable.chickpea),
                new Substitute("Flax Seed Meal", "ing_flax_seed", "Egg replacer", "1 egg → 1 Tbsp flax", R.drawable.flax_seed)
        ));

        // === Steak的纯素替代品 ===
        VEGAN_SUBSTITUTES.put("Steak", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Firm texture", "4 Oz steak → 5 Oz tofu", R.drawable.tofu),
                new Substitute("Tempeh", "ing_tempeh", "Hearty texture", "4 Oz steak → 4.5 Oz tempeh", R.drawable.tempeh),
                new Substitute("Seitan", "ing_seitan", "Meaty texture", "4 Oz steak → 4 Oz seitan", R.drawable.seitan),
                new Substitute("Chickpeas", "ing_chickpeas", "Protein-rich", "4 Oz steak → 6 Oz chickpeas", R.drawable.chickpea)
        ));

        // === Salmon的纯素替代品 ===
        VEGAN_SUBSTITUTES.put("Salmon", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Marinated option", "4 Oz salmon → 5 Oz tofu", R.drawable.tofu),
                new Substitute("Tempeh", "ing_tempeh", "Smoky flavor", "4 Oz salmon → 4 Oz tempeh", R.drawable.tempeh)
        ));

        // === Queso Fresco的纯素替代品 ===
        VEGAN_SUBSTITUTES.put("Queso Fresco", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Crumbled texture", "1 Oz cheese → 1.5 Oz tofu", R.drawable.tofu),
                new Substitute("Nutritional Yeast", "ing_nutritional_yeast", "Cheesy flavor", "1 Oz cheese → 0.5 Oz yeast", R.drawable.nutritional_yeast)
        ));
    }

    private static void initVegetarianSubstitutes() {
        // === Steak的素食替代品 ===
        VEGETARIAN_SUBSTITUTES.put("Steak", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Firm texture", "4 Oz steak → 5 Oz tofu", R.drawable.tofu),
                new Substitute("Tempeh", "ing_tempeh", "Nutty flavor", "4 Oz steak → 4.5 Oz tempeh", R.drawable.tempeh),
                new Substitute("Seitan", "ing_seitan", "High protein", "4 Oz steak → 4 Oz seitan", R.drawable.seitan),
                new Substitute("Chickpeas", "ing_chickpeas", "Protein-rich", "4 Oz steak → 6 Oz chickpeas", R.drawable.chickpea)
        ));

        // === Salmon的素食替代品 ===
        VEGETARIAN_SUBSTITUTES.put("Salmon", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Protein source", "4 Oz salmon → 5 Oz tofu", R.drawable.tofu),
                new Substitute("Tempeh", "ing_tempeh", "Omega-3 fortified", "4 Oz salmon → 4 Oz tempeh", R.drawable.tempeh)
        ));
    }

    private static void initGlutenFreeSubstitutes() {
        // === Flour Tortilla的无麸质替代品 ===
        GLUTEN_FREE_SUBSTITUTES.put("Flour Tortilla", Arrays.asList(
                new Substitute("Corn Tortilla", "ing_corn_tortilla", "Gluten-free wrap", "1:1 replacement", R.drawable.corn_tortilla),
                new Substitute("Corn", "ing_corn", "Deconstructed option", "2 tortillas → 4 Oz corn", R.drawable.corn)
        ));

        // === Brown Rice的替代品（虽然本身无麸质）===
        GLUTEN_FREE_SUBSTITUTES.put("Brown Rice", Arrays.asList(
                new Substitute("Quinoa", "ing_quinoa", "Higher protein", "1 Oz rice → 1 Oz quinoa", R.drawable.quinoa)
        ));

        // === Seitan的无麸质替代（因为Seitan是小麦蛋白）===
        GLUTEN_FREE_SUBSTITUTES.put("Seitan", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Gluten-free protein", "1:1 replacement", R.drawable.tofu),
                new Substitute("Tempeh", "ing_tempeh", "Soy-based protein", "1:1 replacement", R.drawable.tempeh)
        ));
    }

    private static void initAllergenFreeSubstitutes() {
        // === 鸡蛋过敏替代品 ===
        Map<String, List<Substitute>> eggAllergySubs = new HashMap<>();
        eggAllergySubs.put("Egg", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Egg-free protein", "1 egg → 3 Oz tofu", R.drawable.tofu),
                new Substitute("Chickpeas", "ing_chickpeas", "Plant protein", "1 egg → 2 Oz chickpeas", R.drawable.chickpea),
                new Substitute("Flax Seed Meal", "ing_flax_seed", "Baking substitute", "1 egg → 1 Tbsp flax", R.drawable.flax_seed)
        ));
        ALLERGEN_FREE_SUBSTITUTES.put("eggs", eggAllergySubs);

        // === 乳制品过敏替代品 ===
        Map<String, List<Substitute>> dairyAllergySubs = new HashMap<>();
        dairyAllergySubs.put("Queso Fresco", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Dairy-free", "1 Oz cheese → 1.5 Oz tofu", R.drawable.tofu),
                new Substitute("Nutritional Yeast", "ing_nutritional_yeast", "Cheesy flavor", "1 Oz cheese → 0.5 Oz yeast", R.drawable.nutritional_yeast)
        ));
        ALLERGEN_FREE_SUBSTITUTES.put("dairy", dairyAllergySubs);

        // === 大豆过敏替代品 ===
        Map<String, List<Substitute>> soyAllergySubs = new HashMap<>();
        soyAllergySubs.put("Tofu", Arrays.asList(
                new Substitute("Chickpeas", "ing_chickpeas", "Soy-free protein", "2 Oz tofu → 2 Oz chickpeas", R.drawable.chickpea),
                new Substitute("Quinoa", "ing_quinoa", "Complete protein", "2 Oz tofu → 2 Oz quinoa", R.drawable.quinoa),
                new Substitute("Seitan", "ing_seitan", "Wheat protein", "2 Oz tofu → 2 Oz seitan", R.drawable.seitan)
        ));
        soyAllergySubs.put("Tempeh", Arrays.asList(
                new Substitute("Seitan", "ing_seitan", "Soy-free protein", "1:1 replacement", R.drawable.seitan),
                new Substitute("Chickpeas", "ing_chickpeas", "Legume protein", "4 Oz tempeh → 4 Oz chickpeas", R.drawable.chickpea)
        ));
        ALLERGEN_FREE_SUBSTITUTES.put("soy", soyAllergySubs);

        // === 贝类过敏（为未来扩展准备）===
        Map<String, List<Substitute>> shellfishAllergySubs = new HashMap<>();
        shellfishAllergySubs.put("Salmon", Arrays.asList(
                new Substitute("Tofu", "ing_tofu", "Fish-free protein", "4 Oz salmon → 5 Oz tofu", R.drawable.tofu),
                new Substitute("Tempeh", "ing_tempeh", "Savory option", "4 Oz salmon → 4 Oz tempeh", R.drawable.tempeh)
        ));
        ALLERGEN_FREE_SUBSTITUTES.put("shellfish", shellfishAllergySubs);
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

    /**
     * 验证所有替代品的ingredientId是否有效
     * 调试时使用，生产环境可以移除
     */
    public static void validateSubstitutes(android.content.Context context) {
        edu.tamu.csce634.smartshop.data.PresetRepository repo =
                new edu.tamu.csce634.smartshop.data.PresetRepository(context);

        try {
            List<edu.tamu.csce634.smartshop.models.ShoppingItem> allItems = repo.loadInitialItems();
            java.util.Set<String> validIds = new java.util.HashSet<>();

            for (edu.tamu.csce634.smartshop.models.ShoppingItem item : allItems) {
                validIds.add(item.ingredientId);
                android.util.Log.d("SubstituteValidation", "Valid ID: " + item.ingredientId + " (" + item.name + ")");
            }

            android.util.Log.d("SubstituteValidation", "=== Validating VEGAN substitutes ===");
            validateMap(VEGAN_SUBSTITUTES, validIds, "VEGAN");

            android.util.Log.d("SubstituteValidation", "=== Validating VEGETARIAN substitutes ===");
            validateMap(VEGETARIAN_SUBSTITUTES, validIds, "VEGETARIAN");

            android.util.Log.d("SubstituteValidation", "=== Validating GLUTEN_FREE substitutes ===");
            validateMap(GLUTEN_FREE_SUBSTITUTES, validIds, "GLUTEN_FREE");

            android.util.Log.d("SubstituteValidation", "=== Validating ALLERGEN_FREE substitutes ===");
            for (Map.Entry<String, Map<String, List<Substitute>>> entry : ALLERGEN_FREE_SUBSTITUTES.entrySet()) {
                android.util.Log.d("SubstituteValidation", "  Allergen: " + entry.getKey());
                validateMap(entry.getValue(), validIds, "ALLERGEN_FREE[" + entry.getKey() + "]");
            }

            android.util.Log.d("SubstituteValidation", "✅ All substitute IDs are valid!");

        } catch (Exception e) {
            android.util.Log.e("SubstituteValidation", "❌ Validation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void validateMap(Map<String, List<Substitute>> map, java.util.Set<String> validIds, String mapName) {
        for (Map.Entry<String, List<Substitute>> entry : map.entrySet()) {
            String originalIngredient = entry.getKey();
            for (Substitute sub : entry.getValue()) {
                if (!validIds.contains(sub.ingredientId)) {
                    android.util.Log.e("SubstituteValidation",
                            "❌ [" + mapName + "] Invalid ID: " + sub.ingredientId +
                                    " for substitute '" + sub.name + "' (original: " + originalIngredient + ")");
                } else {
                    android.util.Log.d("SubstituteValidation",
                            "✅ [" + mapName + "] " + originalIngredient + " → " + sub.name + " (" + sub.ingredientId + ")");
                }
            }
        }
    }
}