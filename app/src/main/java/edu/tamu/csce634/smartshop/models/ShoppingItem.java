package edu.tamu.csce634.smartshop.models;

// 数据类：描述一个购物条目的所有信息
public class ShoppingItem {
    public String ingredientId;    // 食材ID（唯一标识）
    public String name;            // 食材名称
    public String unit;            // 单位，例如 L / kg
    public double quantity;        // 数量
    public String aisle;           // 门店分区（保留字段，看需要使用）
    public String category;        // 分类（保留字段，看需要使用）

    public double unitPrice;       // 单价
    public String selectedSkuName; // 当前选中的商品名（如 Organic Milk）
    public String skuSpec; // 规格/包装信息（如 "500g"、"1L x 2"）
    public String imageUrl;

    public double coordinateX = -1.0; // 默认值为-1，表示尚未定位
    public double coordinateY = -1.0;
    public String recipeNeededStr;
    public double recipeNeededValue;
    public String recipeNeededUnit;

    private boolean done = false;
    public String getImageUrl() {
        return imageUrl;
    }
    public String getName() {
        return name;
    }

    public String getAisle() {
        return aisle;
    }
    public boolean isDone() {
        return this.done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String originalIngredientId;
    public double substitutionRatio = 1.0;
    public boolean isSubstituted = false;
    public String substituteDisplayName;

    public boolean isPicked = false;

}
