package edu.tamu.csce634.smartshop.ui.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.tamu.csce634.smartshop.models.ShoppingItem;
import edu.tamu.csce634.smartshop.utils.QuantityParser;

/**
 * 购物清单 ViewModel
 *
 * 职责：
 * - 管理购物列表数据和总价
 * - 处理商品替换逻辑（SKU切换、食材替换）
 * - 提供Map模块所需的分组数据接口
 **/
public class ListViewModel extends ViewModel {

    // ========== 核心数据（List模块使用）==========

    private final MutableLiveData<Double> totalLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ShoppingItem>> itemListLiveData = new MutableLiveData<>();
    private List<ShoppingItem> itemList = new ArrayList<>();

    // ========== Map模块接口数据 ==========

    /**
     * 按通道分组的商品数据（Map模块读取）
     * Key: 通道名称（如 "Produce Aisle 4"）
     * Value: 该通道的商品列表
     */
    private final MutableLiveData<Map<String, List<ShoppingItem>>> itemsByAisleLiveData =
            new MutableLiveData<>();

    /**
     * 购物进度数据（Map模块读取）
     */
    private final MutableLiveData<ShoppingProgress> progressLiveData = new MutableLiveData<>();

    // ========== 构造函数 ==========

    public ListViewModel() {
        totalLiveData.setValue(0.0);
        itemListLiveData.setValue(itemList);
        itemsByAisleLiveData.setValue(new LinkedHashMap<>());
        progressLiveData.setValue(new ShoppingProgress(0, 0));
    }

    // ========== List模块使用的方法 ==========

    /**
     * 获取总价LiveData
     */
    public LiveData<Double> getTotal() {
        return totalLiveData;
    }

    /**
     * 获取购物列表LiveData
     */
    public LiveData<List<ShoppingItem>> getItemList() {
        return itemListLiveData;
    }

    /**
     * 设置总价（内部使用）
     */
    public void setTotal(double newTotal) {
        totalLiveData.setValue(newTotal);
    }

    /**
     * 更新购物列表
     **/
    public void updateItemList(List<ShoppingItem> newItemList) {
        this.itemList = newItemList;
        itemListLiveData.setValue(newItemList);
        recalculateTotal(newItemList);
        updateAisleGrouping(newItemList);  // ✅ Phase 6: 自动更新分组
        updateProgress(newItemList);       // ✅ Phase 6: 自动更新进度
    }

    /**
     * 替换商品SKU
     */
    public void replaceSku(String ingredientId, String newSkuName, double newPrice) {
        replaceSkuFull(ingredientId, newSkuName, newPrice, null, null);
    }

    /**
     * 替换商品SKU
     * 更新：名称、价格、规格、图片
     */
    public void replaceSkuFull(String ingredientId,
                               String newSkuName,
                               double newPrice,
                               String newSkuSpec,
                               String newImageUrl) {
        List<ShoppingItem> updated = new ArrayList<>(itemList);
        for (ShoppingItem it : updated) {
            if (ingredientId.equals(it.ingredientId)) {
                if (newSkuName != null) it.selectedSkuName = newSkuName;
                it.unitPrice = newPrice;
                if (newSkuSpec != null) it.skuSpec = newSkuSpec;
                if (newImageUrl != null && !newImageUrl.isEmpty()) it.imageUrl = newImageUrl;
                break;
            }
        }
        updateItemList(updated);
    }

    /**
     * 计算总价
     */
    private void recalculateTotal(List<ShoppingItem> updatedList) {
        double sum = 0.0;
        for (ShoppingItem item : updatedList) {
            sum += item.unitPrice * item.quantity;
        }
        setTotal(sum);
    }

    /**
     * 重新计算某个商品的购买数量（切换Option后调用）
     */
    public void recalculateQuantityForItem(String ingredientId) {
        List<ShoppingItem> updated = new ArrayList<>(itemList);
        for (ShoppingItem item : updated) {
            if (ingredientId.equals(item.ingredientId)) {
                QuantityParser.ParsedQuantity packageParsed =
                        QuantityParser.parse(item.skuSpec);
                if (packageParsed.success && item.recipeNeededValue > 0) {
                    boolean unitMatch = item.recipeNeededUnit.isEmpty() ||
                            packageParsed.unit.isEmpty() ||
                            item.recipeNeededUnit.equalsIgnoreCase(packageParsed.unit);
                    if (unitMatch) {
                        item.quantity = QuantityParser.calculatePackageCount(
                                item.recipeNeededValue,
                                packageParsed.value
                        );
                    } else {
                        item.quantity = 1;
                    }
                } else {
                    item.quantity = 1;
                }
                break;
            }
        }
        updateItemList(updated);
    }

    /**
     * 只重新计算总价，不触发列表LiveData更新（避免滚动重置）
     */
    public void recalculateTotalOnly() {
        List<ShoppingItem> currentList = itemListLiveData.getValue();
        if (currentList != null) {
            recalculateTotal(currentList);
        }
    }

    /**
     * Phase 4: 替换食材为替代品
     *
     * @param originalIngredientId 原始食材ID
     * @param substituteIngredientId 替代品ID
     * @param substituteName 替代品名称
     * @param quantityRatio 用量比例
     * @param presetData 替代品的预置数据
     * @return 是否替换成功
     */
    public boolean replaceIngredient(String originalIngredientId,
                                     String substituteIngredientId,
                                     String substituteName,
                                     double quantityRatio,
                                     ShoppingItem presetData) {
        List<ShoppingItem> updated = new ArrayList<>(itemList);
        boolean found = false;

        for (ShoppingItem item : updated) {
            if (originalIngredientId.equals(item.ingredientId)) {
                found = true;

                // 保存原始ID（首次替换时）
                if (item.originalIngredientId == null) {
                    item.originalIngredientId = item.ingredientId;
                }

                // 更新为替代品
                item.ingredientId = substituteIngredientId;
                item.name = substituteName;
                item.selectedSkuName = presetData.selectedSkuName;
                item.substituteDisplayName = "Replaced with " + substituteName;
                item.isSubstituted = true;

                // 更新价格和规格
                item.unitPrice = presetData.unitPrice;
                item.skuSpec = presetData.skuSpec;
                item.unit = presetData.unit;
                item.aisle = presetData.aisle;

                // 更新图片
                if (presetData.imageUrl != null && !presetData.imageUrl.isEmpty()) {
                    item.imageUrl = presetData.imageUrl;
                }

                // 应用用量比例
                item.substitutionRatio = quantityRatio;
                item.recipeNeededValue = item.recipeNeededValue * quantityRatio;
                item.recipeNeededStr = formatQuantity(item.recipeNeededValue) +
                        (item.recipeNeededUnit.isEmpty() ? "" : " " + item.recipeNeededUnit);

                // 根据新包装规格重新计算购买数量
                QuantityParser.ParsedQuantity packageParsed = QuantityParser.parse(item.skuSpec);
                if (packageParsed.success && item.recipeNeededValue > 0) {
                    boolean unitMatch = item.recipeNeededUnit.isEmpty() ||
                            packageParsed.unit.isEmpty() ||
                            item.recipeNeededUnit.equalsIgnoreCase(packageParsed.unit);

                    if (unitMatch) {
                        item.quantity = QuantityParser.calculatePackageCount(
                                item.recipeNeededValue,
                                packageParsed.value
                        );
                    } else {
                        item.quantity = 1;
                    }
                } else {
                    item.quantity = 1;
                }

                break;
            }
        }

        if (found) {
            updateItemList(updated);
        }

        return found;
    }

    /**
     * 格式化数量显示（去除多余小数）
     */
    private String formatQuantity(double value) {
        if (Math.abs(value - Math.round(value)) < 1e-9) {
            return String.valueOf((int) Math.round(value));
        }
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    // ========== Phase 6: Map模块专用接口 ==========

    /**
     * 【Map接口1】获取按通道分组的商品列表（只读）
     *
     * 返回格式：Map<通道名称, 商品列表>
     * 特性：
     * - 自动过滤 quantity <= 0 的商品
     * - 按通道号排序（Aisle 1, 2, 3...）
     * - 实时更新
     */
    public LiveData<Map<String, List<ShoppingItem>>> getItemsByAisle() {
        return itemsByAisleLiveData;
    }

    /**
     * 【Map接口2】获取购物清单摘要
     *
     * 返回：ShoppingListSummary（总件数、总价、通道数）
     */
    public ShoppingListSummary getSummary() {
        int totalItems = 0;
        int uniqueItems = 0;
        double totalPrice = 0.0;
        java.util.Set<String> aisles = new java.util.HashSet<>();

        for (ShoppingItem item : itemList) {
            if (item.quantity > 0) {
                totalItems += (int) item.quantity;
                uniqueItems++;
                totalPrice += item.quantity * item.unitPrice;
                aisles.add(item.aisle != null ? item.aisle : "General");
            }
        }

        return new ShoppingListSummary(totalItems, uniqueItems, totalPrice, aisles.size());
    }

    /**
     * 【Map接口3】标记商品已拾取
     *
     * @param ingredientId 商品ID
     * @param picked true=已拾取, false=取消拾取
     */
    public void markItemAsPicked(String ingredientId, boolean picked) {
        boolean changed = false;

        for (ShoppingItem item : itemList) {
            if (ingredientId.equals(item.ingredientId)) {
                item.isPicked = picked;
                changed = true;
                break;
            }
        }

        // 只更新进度，不触发列表刷新（避免Map界面重绘）
        if (changed) {
            updateProgress(itemList);
        }
    }

    /**
     * 【Map接口4】获取购物进度
     */
    public LiveData<ShoppingProgress> getProgress() {
        return progressLiveData;
    }

    /**
     * 【Map接口5】清空所有数据（Map模块完成购物后调用）
     */
    public void clearAllData() {
        this.itemList.clear();
        itemListLiveData.setValue(new ArrayList<>());
        itemsByAisleLiveData.setValue(new LinkedHashMap<>());
        totalLiveData.setValue(0.0);
        progressLiveData.setValue(new ShoppingProgress(0, 0));
    }

    // ========== 内部辅助方法 ==========

    /**
     * 按通道分组并排序
     *
     * 逻辑：
     * 1. 过滤 quantity <= 0 的商品
     * 2. 按通道名称分组
     * 3. 提取通道号排序（Aisle 1 < Aisle 2）
     */
    private void updateAisleGrouping(List<ShoppingItem> items) {
        Map<String, List<ShoppingItem>> grouped = new LinkedHashMap<>();

        // 分组
        for (ShoppingItem item : items) {
            if (item.quantity <= 0) continue;  // ✅ 过滤无效商品

            String aisle = item.aisle != null && !item.aisle.isEmpty()
                    ? item.aisle : "General";

            grouped.computeIfAbsent(aisle, k -> new ArrayList<>()).add(item);
        }

        // 排序（按通道号）
        Map<String, List<ShoppingItem>> sorted = new LinkedHashMap<>();
        grouped.entrySet().stream()
                .sorted((e1, e2) -> compareAisle(e1.getKey(), e2.getKey()))
                .forEachOrdered(e -> sorted.put(e.getKey(), e.getValue()));

        itemsByAisleLiveData.setValue(sorted);
    }

    /**
     * 比较通道顺序（提取数字）
     * 例如："Produce Aisle 4" → 4
     */
    private int compareAisle(String a1, String a2) {
        int num1 = extractAisleNumber(a1);
        int num2 = extractAisleNumber(a2);
        return Integer.compare(num1, num2);
    }

    /**
     * 从通道名称中提取数字
     */
    private int extractAisleNumber(String aisle) {
        try {
            String[] parts = aisle.split("\\s+");
            for (String part : parts) {
                if (part.matches("\\d+")) {
                    return Integer.parseInt(part);
                }
            }
        } catch (Exception e) {
            // 解析失败返回最大值（排在最后）
        }
        return 999;
    }

    /**
     * 更新购物进度（Map模块使用）
     *
     * 计算：已拾取数量 / 总数量
     */
    private void updateProgress(List<ShoppingItem> items) {
        int total = 0;
        int picked = 0;

        for (ShoppingItem item : items) {
            if (item.quantity > 0) {
                total++;
                if (item.isPicked) {
                    picked++;
                }
            }
        }

        progressLiveData.setValue(new ShoppingProgress(picked, total));
    }

    // ========== 数据模型类 ==========

    /**
     * 购物清单摘要（Map模块使用）
     */
    public static class ShoppingListSummary {
        public int totalItems;      // 总购买件数（如：5件）
        public int uniqueItems;     // 不同商品数（如：3种）
        public double totalPrice;   // 总价（如：19.43）
        public int aisleCount;      // 涉及通道数（如：3个）

        public ShoppingListSummary(int totalItems, int uniqueItems,
                                   double totalPrice, int aisleCount) {
            this.totalItems = totalItems;
            this.uniqueItems = uniqueItems;
            this.totalPrice = totalPrice;
            this.aisleCount = aisleCount;
        }
    }

    /**
     * 购物进度（Map模块使用）
     */
    public static class ShoppingProgress {
        public int pickedCount;     // 已拾取数量
        public int totalCount;      // 总商品数
        public int percentage;      // 完成百分比（0-100）

        public ShoppingProgress(int picked, int total) {
            this.pickedCount = picked;
            this.totalCount = total;
            this.percentage = total > 0 ? (picked * 100 / total) : 0;
        }
    }
}
