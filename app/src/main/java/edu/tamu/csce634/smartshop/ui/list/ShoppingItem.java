package edu.tamu.csce634.smartshop.ui.list;

// 数据类：描述一个购物条目的所有信息
public class ShoppingItem {
    public String ingredientId;    // 食材ID（唯一标识）
    public String name;            // 食材名称
    public String unit;            // 单位，例如 L / kg
    public double quantity;        // 数量
    public String aisle;           // 超市货架分区
    public double unitPrice;       // 单价
    public String selectedSkuName; // 当前选中的商品名
}
