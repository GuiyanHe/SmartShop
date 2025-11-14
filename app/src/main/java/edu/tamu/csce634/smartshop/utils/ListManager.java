package edu.tamu.csce634.smartshop.utils;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.tamu.csce634.smartshop.models.ShoppingItem;

/**
 * 购物清单管理器（单例模式）
 *
 * 职责：
 * - 管理购物列表数据（List模块和Map模块共享）
 * - 提供LiveData供UI观察
 * - 提供只读接口给Map模块
 * - 管理数据清空逻辑
 *
 * Phase 6 实现
 */
public class ListManager {

    private static ListManager instance;
    private Context applicationContext;

    // 核心数据
    private final MutableLiveData<List<ShoppingItem>> itemListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Double> totalPriceLiveData = new MutableLiveData<>();
    private final MutableLiveData<Map<String, List<ShoppingItem>>> itemsByAisleLiveData =
            new MutableLiveData<>();
    private final MutableLiveData<ShoppingProgress> progressLiveData = new MutableLiveData<>();

    private List<ShoppingItem> itemList = new ArrayList<>();

    // 私有构造函数
    private ListManager(Context context) {
        this.applicationContext = context.getApplicationContext();
        totalPriceLiveData.setValue(0.0);
        itemListLiveData.setValue(new ArrayList<>());
        itemsByAisleLiveData.setValue(new LinkedHashMap<>());
        progressLiveData.setValue(new ShoppingProgress(0, 0));
    }

    /**
     * 获取单例实例
     */
    public static synchronized ListManager getInstance(Context context) {
        if (instance == null) {
            instance = new ListManager(context);
        }
        return instance;
    }

    // ========== List模块使用的接口 ==========

    /**
     * 更新购物列表（List模块专用）
     */
    public void updateItemList(List<ShoppingItem> newItemList) {
        this.itemList = new ArrayList<>(newItemList);
        itemListLiveData.setValue(this.itemList);
        recalculateTotal();
        updateAisleGrouping();
        updateProgress();
    }

    /**
     * 获取购物列表LiveData
     */
    public LiveData<List<ShoppingItem>> getItemList() {
        return itemListLiveData;
    }

    /**
     * 获取总价LiveData
     */
    public LiveData<Double> getTotalPrice() {
        return totalPriceLiveData;
    }

    /**
     * 重新计算总价（不触发列表更新）
     */
    public void recalculateTotalOnly() {
        recalculateTotal();
    }

    // ========== Map模块使用的接口 ==========

    /**
     * 【Map接口1】获取按通道分组的商品列表（只读）
     *
     * 数据格式：
     * {
     *   "Produce Aisle 4": [ShoppingItem, ShoppingItem],
     *   "Grains Aisle": [ShoppingItem]
     * }
     *
     * 特性：
     * - 自动过滤 quantity <= 0 的商品
     * - 按通道号排序
     */
    public LiveData<Map<String, List<ShoppingItem>>> getItemsByAisle() {
        return itemsByAisleLiveData;
    }

    /**
     * 【Map接口2】获取购物清单摘要
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

        if (changed) {
            updateProgress();
        }
    }

    /**
     * 【Map接口4】获取购物进度
     */
    public LiveData<ShoppingProgress> getProgress() {
        return progressLiveData;
    }

    /**
     * 【Map接口5】清空所有数据（完成购物后调用）
     */
    public void clearAllData() {
        itemList.clear();
        itemListLiveData.setValue(new ArrayList<>());
        itemsByAisleLiveData.setValue(new LinkedHashMap<>());
        totalPriceLiveData.setValue(0.0);
        progressLiveData.setValue(new ShoppingProgress(0, 0));
    }

    // ========== 内部方法 ==========

    private void recalculateTotal() {
        double sum = 0.0;
        for (ShoppingItem item : itemList) {
            sum += item.unitPrice * item.quantity;
        }
        totalPriceLiveData.setValue(sum);
    }

    private void updateAisleGrouping() {
        Map<String, List<ShoppingItem>> grouped = new LinkedHashMap<>();

        for (ShoppingItem item : itemList) {
            if (item.quantity <= 0) continue;
            String aisle = item.aisle != null && !item.aisle.isEmpty()
                    ? item.aisle : "General";
            grouped.computeIfAbsent(aisle, k -> new ArrayList<>()).add(item);
        }

        // 按通道号排序
        Map<String, List<ShoppingItem>> sorted = new LinkedHashMap<>();
        grouped.entrySet().stream()
                .sorted((e1, e2) -> compareAisle(e1.getKey(), e2.getKey()))
                .forEachOrdered(e -> sorted.put(e.getKey(), e.getValue()));

        itemsByAisleLiveData.setValue(sorted);
    }

    private int compareAisle(String a1, String a2) {
        return Integer.compare(extractAisleNumber(a1), extractAisleNumber(a2));
    }

    private int extractAisleNumber(String aisle) {
        try {
            for (String part : aisle.split("\\s+")) {
                if (part.matches("\\d+")) {
                    return Integer.parseInt(part);
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return 999;
    }

    private void updateProgress() {
        int total = 0;
        int picked = 0;

        for (ShoppingItem item : itemList) {
            if (item.quantity > 0) {
                total++;
                if (item.isPicked) picked++;
            }
        }

        progressLiveData.setValue(new ShoppingProgress(picked, total));
    }

    // ========== 数据模型 ==========

    /**
     * 购物清单摘要
     */
    public static class ShoppingListSummary {
        public int totalItems;      // 总购买件数
        public int uniqueItems;     // 不同商品数
        public double totalPrice;   // 总价
        public int aisleCount;      // 涉及通道数

        public ShoppingListSummary(int totalItems, int uniqueItems,
                                   double totalPrice, int aisleCount) {
            this.totalItems = totalItems;
            this.uniqueItems = uniqueItems;
            this.totalPrice = totalPrice;
            this.aisleCount = aisleCount;
        }
    }

    /**
     * 购物进度
     */
    public static class ShoppingProgress {
        public int pickedCount;
        public int totalCount;
        public int percentage;

        public ShoppingProgress(int picked, int total) {
            this.pickedCount = picked;
            this.totalCount = total;
            this.percentage = total > 0 ? (picked * 100 / total) : 0;
        }
    }
}