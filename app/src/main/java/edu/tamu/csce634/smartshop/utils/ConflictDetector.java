package edu.tamu.csce634.smartshop.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tamu.csce634.smartshop.models.ProfileData;
import edu.tamu.csce634.smartshop.models.ShoppingItem;

/**
 * 冲突检测工具：检测购物清单与用户偏好的冲突
 */
public class ConflictDetector {

    /**
     * 冲突类型
     */
    public enum ConflictType {
        ALLERGEN,      // 过敏源冲突（最高优先级）
        VEGAN,         // 纯素冲突
        VEGETARIAN,    // 素食冲突
        GLUTEN         // 无麸质冲突
    }

    /**
     * 冲突信息
     */
    public static class Conflict {
        public ShoppingItem item;           // 冲突的食材
        public ConflictType type;           // 冲突类型
        public String reason;               // 冲突原因（显示给用户）
        public List<String> conflictDetails; // 具体冲突细节（如过敏源列表）
        public boolean resolved = false;    // 是否已解决

        public Conflict(ShoppingItem item, ConflictType type, String reason) {
            this.item = item;
            this.type = type;
            this.reason = reason;
            this.conflictDetails = new ArrayList<>();
        }
    }

    /**
     * 检测所有冲突
     *
     * @param items 购物清单
     * @param profile 用户偏好
     * @return 冲突列表（按优先级排序）
     */
    public static List<Conflict> detectConflicts(List<ShoppingItem> items, ProfileData profile) {
        if (items == null || profile == null) {
            return new ArrayList<>();
        }

        List<Conflict> conflicts = new ArrayList<>();

        for (ShoppingItem item : items) {
            if (item == null || item.name == null) continue;

            // 1. 检测过敏源冲突（最高优先级）
            List<String> allergies = profile.getAllergiesList();
            List<String> foundAllergens = new ArrayList<>();

            for (String allergen : allergies) {
                if (containsAllergen(item, allergen)) {
                    foundAllergens.add(allergen);
                }
            }

            if (!foundAllergens.isEmpty()) {
                Conflict c = new Conflict(item, ConflictType.ALLERGEN,
                        buildAllergenReason(foundAllergens));
                c.conflictDetails = foundAllergens;
                conflicts.add(c);
                continue; // 过敏源冲突最严重，不再检查其他冲突
            }

            // 2. 检测纯素冲突
            if (profile.isVegan() && !isVeganFriendly(item)) {
                Conflict c = new Conflict(item, ConflictType.VEGAN,
                        "Not suitable for vegan diet");
                conflicts.add(c);
                continue;
            }

            // 3. 检测素食冲突
            if (profile.isVegetarian() && !isVegetarian(item)) {
                Conflict c = new Conflict(item, ConflictType.VEGETARIAN,
                        "Not suitable for vegetarian diet");
                conflicts.add(c);
                continue;
            }

            // 4. 检测无麸质冲突
            if (profile.isGlutenFree() && containsGluten(item)) {
                Conflict c = new Conflict(item, ConflictType.GLUTEN,
                        "Contains gluten");
                conflicts.add(c);
            }
        }

        return conflicts;
    }

    /**
     * 构建过敏源原因文本（支持多个过敏源显示在一行）
     */
    private static String buildAllergenReason(List<String> allergens) {
        if (allergens.size() == 1) {
            return "⚠ Contains allergen: " + capitalizeFirst(allergens.get(0));
        } else {
            StringBuilder sb = new StringBuilder("⚠ Contains allergens: ");
            for (int i = 0; i < allergens.size(); i++) {
                sb.append(capitalizeFirst(allergens.get(i)));
                if (i < allergens.size() - 1) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }
    }

    /**
     * 检测食材是否包含指定过敏源
     */
    private static boolean containsAllergen(ShoppingItem item, String allergen) {
        String name = item.name.toLowerCase();
        allergen = allergen.toLowerCase();

        // 定义过敏源关键词映射
        Map<String, List<String>> allergenKeywords = new HashMap<>();
        allergenKeywords.put("nuts", Arrays.asList("almond", "peanut", "walnut", "cashew", "pecan", "hazelnut"));
        allergenKeywords.put("shellfish", Arrays.asList("shrimp", "crab", "lobster", "shellfish", "prawn"));
        allergenKeywords.put("dairy", Arrays.asList("milk", "cheese", "butter", "yogurt", "cream", "queso"));
        allergenKeywords.put("soy", Arrays.asList("soy", "tofu", "tempeh", "edamame"));
        allergenKeywords.put("eggs", Arrays.asList("egg"));

        List<String> keywords = allergenKeywords.getOrDefault(allergen, Arrays.asList(allergen));

        for (String keyword : keywords) {
            if (name.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检测是否适合纯素饮食
     */
    private static boolean isVeganFriendly(ShoppingItem item) {
        String name = item.name.toLowerCase();
        String[] nonVegan = {"egg", "milk", "cheese", "butter", "yogurt", "cream",
                "chicken", "beef", "steak", "salmon", "fish", "meat",
                "pork", "lamb", "turkey", "honey", "queso"};

        for (String keyword : nonVegan) {
            if (name.contains(keyword)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检测是否适合素食
     */
    private static boolean isVegetarian(ShoppingItem item) {
        String name = item.name.toLowerCase();
        String[] nonVeg = {"chicken", "beef", "steak", "salmon", "fish",
                "meat", "pork", "lamb", "turkey", "seafood"};

        for (String keyword : nonVeg) {
            if (name.contains(keyword)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 检测是否包含麸质
     */
    private static boolean containsGluten(ShoppingItem item) {
        String name = item.name.toLowerCase();
        String[] glutenSources = {"bread", "flour", "pasta", "wheat", "barley",
                "rye", "tortilla", "cereal", "noodle"};

        for (String keyword : glutenSources) {
            if (name.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 首字母大写
     */
    private static String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}