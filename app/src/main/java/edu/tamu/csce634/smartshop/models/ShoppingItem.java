package edu.tamu.csce634.smartshop.models;

// 数据类：描述一个购物条目的所有信息
public class ShoppingItem {
    public String ingredientId;    // 食材ID（唯一标识）
    public String name;            // 食材名称
    public String unit;            // 单位，例如 L / kg
    public double quantity;        // 数量
    public String aisle;           // 门店分区（保留字段，看需要使用）
    public double unitPrice;       // 单价
    public String selectedSkuName; // 当前选中的商品名（如 Organic Milk）
    public String skuSpec; // 规格/包装信息（如 "500g"、"1L x 2"）
    public String imageUrl;

    // Recipe需求量（原始字符串，如 "0.2 Oz"）
    public String recipeNeededStr;
    // 解析后的Recipe需求量数值（如 0.2）
    public double recipeNeededValue;
    // Recipe需求单位（如 "Oz"）
    public String recipeNeededUnit;
}
