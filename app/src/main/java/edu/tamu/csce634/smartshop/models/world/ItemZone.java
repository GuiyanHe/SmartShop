package edu.tamu.csce634.smartshop.models.world;

import com.google.gson.annotations.SerializedName;

// 描述货架上的一块区域
public class ItemZone {

    // 这个区域对应的商品类别 (必须与 options.json 中的 "category" 字段匹配)
    @SerializedName("category")
    public String category;

    // 这个区域在货架上的起始位置 (0.0 to 1.0)
    @SerializedName("start")
    public float start;

    // 这个区域在货架上的结束位置 (0.0 to 1.0)
    @SerializedName("end")
    public float end;
}
