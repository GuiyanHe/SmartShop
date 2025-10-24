package edu.tamu.csce634.smartshop.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 首次启动时把 res/raw 中的预置 JSON 读入 SharedPreferences（只做一次）
 * 之后业务统一从 SharedPreferences 读取，符合 README 的约束
 */
public class DataSeeder {

    // SharedPreferences 名称与键
    public static final String PREF_NAME = "smartshop";
    public static final String KEY_ITEMS_JSON = "items_json";
    public static final String KEY_OPTIONS_JSON = "options_json";
    public static final String KEY_SEEDED = "seed_done_v1"; // 是否已灌入的标记

    /** 对外入口：如未灌入，则从 raw 读入并写入 SP */
    public static void seedIfNeeded(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        if (sp.getBoolean(KEY_SEEDED, false)) return; // 做过就不再重复

        try {
            // 这里的 R.raw.preset_items / preset_options 取决于你的文件名与放置目录
            String items = readRawAsString(ctx, edu.tamu.csce634.smartshop.R.raw.preset_items);
            String options = readRawAsString(ctx, edu.tamu.csce634.smartshop.R.raw.preset_options);

            // 粗校验 JSON 结构（必须包含 items / options 数组）
            new JSONObject(items).getJSONArray("items");
            new JSONObject(options).getJSONArray("options");

            sp.edit()
                    .putString(KEY_ITEMS_JSON, items)
                    .putString(KEY_OPTIONS_JSON, options)
                    .putBoolean(KEY_SEEDED, true)
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String readRawAsString(Context ctx, int rawId) throws Exception {
        InputStream is = ctx.getResources().openRawResource(rawId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append('\n');
        br.close();
        is.close();
        return sb.toString();
    }
}
