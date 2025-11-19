package edu.tamu.csce634.smartshop.models.world;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// 描述整个超市的布局结构
public class SupermarketLayout {

    @SerializedName("supermarket_name")
    public String supermarketName;

    // 包含的所有货架和区域
    @SerializedName("aisles")
    public List<Aisle> aisles;
}
