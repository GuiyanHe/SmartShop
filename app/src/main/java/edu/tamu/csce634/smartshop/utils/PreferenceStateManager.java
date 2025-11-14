package edu.tamu.csce634.smartshop.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 偏好模式状态管理器
 * 负责保存和恢复偏好模式下的购物列表状态
 *
 * Phase 5 实现
 */
public class PreferenceStateManager {

    private static final String PREFS_NAME = "SmartShopPreferenceState";
    private static final String KEY_DEFAULT_QUANTITIES = "default_quantities";
    private static final String KEY_PREFERENCE_QUANTITIES = "preference_quantities";
    private static final String KEY_RESOLUTIONS = "resolution_actions";
    private static final String KEY_SUBSTITUTIONS = "substitution_map";

    private final SharedPreferences prefs;
    private final Gson gson;

    /**
     * 冲突解决类型
     */
    public enum ResolutionType {
        UNRESOLVED,    // 未解决
        SET_TO_ZERO,   // 设为0
        REPLACED       // 已替换
    }

    /**
     * 冲突解决记录
     */
    public static class ResolutionRecord {
        public String ingredientId;          // 原始食材ID
        public ResolutionType type;          // 解决类型
        public String substituteId;          // 替代品ID（仅type=REPLACED时有效）
        public String substituteName;        // 替代品名称
        public int originalQuantity;         // 原始数量（用于恢复）
        public double substitutionRatio;     // 用量比例

        public ResolutionRecord() {}

        public ResolutionRecord(String ingredientId, ResolutionType type) {
            this.ingredientId = ingredientId;
            this.type = type;
        }
    }

    public PreferenceStateManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    // ========== 数量管理 ==========

    /**
     * 保存默认模式的数量快照
     */
    public void saveDefaultQuantities(Map<String, Integer> quantities) {
        String json = gson.toJson(quantities);
        prefs.edit().putString(KEY_DEFAULT_QUANTITIES, json).apply();
    }

    /**
     * 保存偏好模式的数量快照
     */
    public void savePreferenceQuantities(Map<String, Integer> quantities) {
        String json = gson.toJson(quantities);
        prefs.edit().putString(KEY_PREFERENCE_QUANTITIES, json).apply();
    }

    /**
     * 恢复默认模式的数量
     */
    public Map<String, Integer> loadDefaultQuantities() {
        return loadQuantitiesFromKey(KEY_DEFAULT_QUANTITIES);
    }

    /**
     * 恢复偏好模式的数量
     */
    public Map<String, Integer> loadPreferenceQuantities() {
        return loadQuantitiesFromKey(KEY_PREFERENCE_QUANTITIES);
    }

    private Map<String, Integer> loadQuantitiesFromKey(String key) {
        String json = prefs.getString(key, null);
        if (json == null) {
            return new HashMap<>();
        }

        try {
            Type type = new TypeToken<Map<String, Integer>>(){}.getType();
            Map<String, Integer> result = gson.fromJson(json, type);
            return result != null ? result : new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    // ========== 冲突解决管理 ==========

    /**
     * 记录冲突解决方案
     */
    public void recordResolution(String ingredientId, ResolutionType type,
                                 String substituteId, String substituteName,
                                 int originalQuantity, double ratio) {
        Map<String, ResolutionRecord> resolutions = loadResolutions();

        ResolutionRecord record = new ResolutionRecord(ingredientId, type);
        record.substituteId = substituteId;
        record.substituteName = substituteName;
        record.originalQuantity = originalQuantity;
        record.substitutionRatio = ratio;

        resolutions.put(ingredientId, record);
        saveResolutions(resolutions);
    }

    /**
     * 获取某个食材的解决方案
     */
    public ResolutionRecord getResolution(String ingredientId) {
        Map<String, ResolutionRecord> resolutions = loadResolutions();
        return resolutions.get(ingredientId);
    }

    /**
     * 获取所有解决方案
     */
    public Map<String, ResolutionRecord> loadResolutions() {
        String json = prefs.getString(KEY_RESOLUTIONS, null);
        if (json == null) {
            return new HashMap<>();
        }

        try {
            Type type = new TypeToken<Map<String, ResolutionRecord>>(){}.getType();
            Map<String, ResolutionRecord> result = gson.fromJson(json, type);
            return result != null ? result : new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private void saveResolutions(Map<String, ResolutionRecord> resolutions) {
        String json = gson.toJson(resolutions);
        prefs.edit().putString(KEY_RESOLUTIONS, json).apply();
    }

    /**
     * 清除所有解决方案（切换回默认模式时调用）
     */
    public void clearResolutions() {
        prefs.edit().remove(KEY_RESOLUTIONS).apply();
    }

    // ========== 替换关系管理 ==========

    /**
     * 记录替换关系（原始ID → 替代品ID）
     */
    public void recordSubstitution(String originalId, String substituteId) {
        Map<String, String> substitutions = loadSubstitutions();
        substitutions.put(originalId, substituteId);
        saveSubstitutions(substitutions);
    }

    /**
     * 获取替代品ID
     */
    public String getSubstituteId(String originalId) {
        Map<String, String> substitutions = loadSubstitutions();
        return substitutions.get(originalId);
    }

    private Map<String, String> loadSubstitutions() {
        String json = prefs.getString(KEY_SUBSTITUTIONS, null);
        if (json == null) {
            return new HashMap<>();
        }

        try {
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> result = gson.fromJson(json, type);
            return result != null ? result : new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private void saveSubstitutions(Map<String, String> substitutions) {
        String json = gson.toJson(substitutions);
        prefs.edit().putString(KEY_SUBSTITUTIONS, json).apply();
    }

    /**
     * 清除所有数据（用于测试或重置）
     */
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}