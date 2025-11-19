package edu.tamu.csce634.smartshop.models.world;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// 描述一个货架或一个独立的区域
public class Aisle {

    @SerializedName("id")
    public String id; // 唯一的ID，例如 "Aisle_5" 或 "Produce_Area"

    // 描述该区域在地图上的位置和大小 (百分比)
    @SerializedName("x")
    public float x;
    @SerializedName("y")
    public float y;
    @SerializedName("width")
    public float width;
    @SerializedName("height")
    public float height;

    // 包含的商品区列表
    @SerializedName("item_zones")
    public List<ItemZone> itemZones;
}
