package edu.tamu.csce634.smartshop.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.tamu.csce634.smartshop.models.ShoppingItem;

/**
 * 从 SharedPreferences 读取预置 items/options
 * - loadInitialItems(): 返回展开了“默认 option”的 ShoppingItem 列表
 * - getOptionsFor(ingredientId): 返回该食材的所有 SKU（JSONObject 列表）
 */
public class PresetRepository {

    private final SharedPreferences sp;

    public PresetRepository(Context ctx) {
        this.sp = ctx.getSharedPreferences(DataSeeder.PREF_NAME, Context.MODE_PRIVATE);
    }

    /** 初始列表：把默认 option 写进 ShoppingItem 的 selectedSkuName / skuSpec / unitPrice / imageUrl */
    public List<ShoppingItem> loadInitialItems() throws Exception {
        String itemsStr = sp.getString(DataSeeder.KEY_ITEMS_JSON, "{}");
        String optsStr = sp.getString(DataSeeder.KEY_OPTIONS_JSON, "{}");

        JSONArray items = new JSONObject(itemsStr).getJSONArray("items");
        JSONArray options = new JSONObject(optsStr).getJSONArray("options");

        // 建 index：ingredientId -> options[]
        HashMap<String, List<JSONObject>> optIndex = new HashMap<>();
        for (int i = 0; i < options.length(); i++) {
            JSONObject o = options.getJSONObject(i);
            String ingId = o.getString("ingredientId");
            optIndex.computeIfAbsent(ingId, k -> new ArrayList<>()).add(o);
        }

        List<ShoppingItem> list = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject it = items.getJSONObject(i);
            String ingId = it.getString("ingredientId");

            String defaultOptId = it.optString("defaultOptionId", "");
            JSONObject selected = null;
            List<JSONObject> candidates = optIndex.getOrDefault(ingId, new ArrayList<>());
            for (JSONObject c : candidates) {
                if (c.optString("optionId").equals(defaultOptId)) {
                    selected = c;
                    break;
                }
            }
            if (selected == null && !candidates.isEmpty()) selected = candidates.get(0);

            ShoppingItem s = new ShoppingItem();
            s.ingredientId = ingId;
            s.name         = it.optString("name", "");
            s.unit         = it.optString("unit", "");
            s.quantity     = it.optDouble("defaultQuantity", 1);
            s.aisle        = it.optString("aisle", "");
            s.imageUrl     = it.optString("imageUrl", ""); // 类别图（兜底）

            if (selected != null) {
                s.selectedSkuName = selected.optString("displayName", s.name);
                s.skuSpec         = selected.optString("size", "");
                s.unitPrice       = selected.optDouble("unitPrice", 0);
                String skuImg     = selected.optString("imageUrl", "");
                if (!skuImg.isEmpty()) s.imageUrl = skuImg; // 优先用 SKU 图
            }

            list.add(s);
        }
        return list;
    }

    /** 返回某个食材的所有具体 SKU（用于 BottomSheet 展示） */
    public List<JSONObject> getOptionsFor(String ingredientId) throws Exception {
        String optsStr = sp.getString(DataSeeder.KEY_OPTIONS_JSON, "{}");
        JSONArray options = new JSONObject(optsStr).getJSONArray("options");

        List<JSONObject> result = new ArrayList<>();
        for (int i = 0; i < options.length(); i++) {
            JSONObject o = options.getJSONObject(i);
            if (ingredientId.equals(o.optString("ingredientId"))) {
                result.add(o);
            }
        }
        return result;
    }
}
