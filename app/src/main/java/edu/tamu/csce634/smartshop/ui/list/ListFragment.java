package edu.tamu.csce634.smartshop.ui.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tamu.csce634.smartshop.adapters.ShoppingItemAdapter;
import edu.tamu.csce634.smartshop.databinding.FragmentListBinding;
import edu.tamu.csce634.smartshop.data.DataSeeder;
import edu.tamu.csce634.smartshop.data.PresetRepository;
import edu.tamu.csce634.smartshop.models.ShoppingItem;
import edu.tamu.csce634.smartshop.ui.recipe.RecipeViewModel;
import edu.tamu.csce634.smartshop.utils.QuantityParser;

/**
 * 购物清单页面：
 * - 首次启动把 res/raw 的预置 JSON 灌入 SharedPreferences
 * - 从预置数据生成初始列表（默认 Breakfast 配方）
 * - 底部提供 3 个 recipe 切换按钮（Quick Breakfast / Family Dinner / Vegan Bowl）
 * - 顶部右侧显示实时总价与 Map 按钮（Map 按钮仅打印日志）
 * - 与 ListViewModel/ShoppingItemAdapter 协作，支持 replaceSkuFull、数量 ± 等
 */
public class ListFragment extends Fragment {

    // ViewBinding：对应 fragment_list.xml
    private FragmentListBinding binding;

    // VM / 适配器 / 数据仓库
    private ListViewModel listViewModel;
    private RecipeViewModel recipeViewModel;
    private ShoppingItemAdapter adapter;
    private PresetRepository repo;

//    // 三套示例配方（ingredientId 与 preset_items.json 对应）
//    private static final List<String> RECIPE_BREAKFAST = Arrays.asList(
//            "ing_milk","ing_eggs","ing_bread","ing_butter","ing_coffee","ing_banana"
//    );
//    private static final List<String> RECIPE_FAMILY_DINNER = Arrays.asList(
//            "ing_chicken","ing_rice","ing_tomato","ing_onion","ing_garlic","ing_lettuce","ing_olive_oil"
//    );
//    private static final List<String> RECIPE_VEGAN = Arrays.asList(
//            "ing_spinach","ing_potato","ing_carrot","ing_cucumber","ing_rice","ing_oatmeal","ing_apple"
//    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 使用 ViewBinding 加载布局
        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) 首次把 res/raw 的预置 JSON 灌入 SharedPreferences（只做一次）
        DataSeeder.seedIfNeeded(requireContext());
        repo = new PresetRepository(requireContext());

        // 2) 获取 ViewModel（使用 Activity 作用域便于 BottomSheet 共享）
        listViewModel = new ViewModelProvider(requireActivity()).get(ListViewModel.class);
        recipeViewModel = new ViewModelProvider(requireActivity()).get(RecipeViewModel.class);

        recipeViewModel.init(requireContext());
        // 3) RecyclerView 基本配置
        RecyclerView recyclerView = binding.recycler;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        // 先给空数据，后续通过 LiveData 驱动
        adapter = new ShoppingItemAdapter(new ArrayList<>(), listViewModel);
        recyclerView.setAdapter(adapter);

        // 4) 绑定总价显示
        TextView totalText = binding.totalText;
        listViewModel.getTotal().observe(getViewLifecycleOwner(), total -> {
            // 获取当前购物车商品数量
            int itemCount = listViewModel.getItemList().getValue() != null ?
                    listViewModel.getItemList().getValue().size() : 0;

            if (itemCount > 0) {
                String priceText = String.format("Estimated Total: ¥%.2f (%d items)", total, itemCount);
                totalText.setText(priceText);
            } else {
                totalText.setText("Estimated Total: ¥0.00");
            }
        });
        // 5) 观察列表变化，刷新适配器
        listViewModel.getItemList().observe(getViewLifecycleOwner(), list -> {
            adapter = new ShoppingItemAdapter(list, listViewModel);
            recyclerView.setAdapter(adapter);
        });

        // 6) 观察购物车聚合食材数据
        recipeViewModel.getRequiredIngredients().observe(getViewLifecycleOwner(), merged -> {
            if (merged != null && !merged.isEmpty()) {
                // 有数据：隐藏空状态，显示列表
                binding.emptyStateLayout.setVisibility(View.GONE);
                binding.recycler.setVisibility(View.VISIBLE);

                // 转换聚合食材为购物清单
                convertCartToShoppingList(merged);
//                android.util.Log.d("ListFragment", "Loaded " + merged.size() + " ingredients from cart");
            } else {
                // 购物车为空：显示空状态，隐藏列表
                binding.emptyStateLayout.setVisibility(View.VISIBLE);
                binding.recycler.setVisibility(View.GONE);

                listViewModel.updateItemList(new ArrayList<>());
//                android.util.Log.d("ListFragment", "Cart is empty, showing empty state");
            }
        });

        // 7) 默认加载：Breakfast 配方
//        loadRecipe(RECIPE_BREAKFAST);
        listViewModel.updateItemList(new ArrayList<>());

        // 7) 配方切换按钮
//        binding.btnRecipeBreakfast.setOnClickListener(v -> loadRecipe(RECIPE_BREAKFAST));
//        binding.btnRecipeDinner.setOnClickListener(v -> loadRecipe(RECIPE_FAMILY_DINNER));
//        binding.btnRecipeVegan.setOnClickListener(v -> loadRecipe(RECIPE_VEGAN));

        // 8) 底部“Proceed to Map”
        binding.btnProceed.setOnClickListener(v ->
                totalText.setText("Navigating to Store Map… (demo)")
        );


    }

//    /** 按配方筛选 items 并交给 ViewModel（会自动重算总价并刷新 UI） */
//    private void loadRecipe(List<String> ingredientIds) {
//        try {
//            List<ShoppingItem> all = repo.loadInitialItems();  // 带默认 option
//            List<ShoppingItem> filtered = new ArrayList<>();
//            for (ShoppingItem it : all) {
//                if (ingredientIds.contains(it.ingredientId)) {
//                    filtered.add(it);
//                }
//            }
//            listViewModel.updateItemList(filtered);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    /**
     * 将购物车聚合食材转换为ShoppingItem列表
     * 使用Recipe模块中定义的食材图片
     */
    private void convertCartToShoppingList(Map<String, String> mergedIngredients) {
        try {
            // 获取所有Recipe对象
            List<edu.tamu.csce634.smartshop.models.Recipe> recipes =
                    recipeViewModel.getRecipes().getValue();

            if (recipes == null) {
                recipes = new ArrayList<>();
            }

            // 构建食材名称到图片资源的映射
            Map<String, Integer> ingredientImageMap = new HashMap<>();
            for (edu.tamu.csce634.smartshop.models.Recipe recipe : recipes) {
                for (edu.tamu.csce634.smartshop.models.Ingredient ingredient : recipe.getIngredients()) {
                    String name = ingredient.getName().toLowerCase().trim();
                    ingredientImageMap.put(name, ingredient.getImageResId());
                }
            }

// 获取预置数据作为价格等信息的参考
            List<ShoppingItem> presetItems = repo.loadInitialItems();
//            for (ShoppingItem p : presetItems) {
//                android.util.Log.d("ListFragment", "Preset loaded: " + p.name +
//                        " | SKU: " + p.selectedSkuName +
//                        " | Spec: " + p.skuSpec +
//                        " | Price: " + p.unitPrice);
//            }
            Map<String, ShoppingItem> presetMap = new HashMap<>();

// 建立多种映射方式
            for (ShoppingItem preset : presetItems) {
                String normalizedName = preset.name.toLowerCase().trim();

                // 方式1：直接名称匹配
                presetMap.put(normalizedName, preset);

                // 方式2：移除空格后匹配（如 "Brown Rice" -> "brownrice"）
                presetMap.put(normalizedName.replaceAll("\\s+", ""), preset);

                // 方式3：使用 selectedSkuName
                if (preset.selectedSkuName != null) {
                    String normalizedSku = preset.selectedSkuName.toLowerCase().trim();
                    presetMap.put(normalizedSku, preset);
                    presetMap.put(normalizedSku.replaceAll("\\s+", ""), preset);
                }
            }

//            android.util.Log.d("ListFragment", "Built preset map with " + presetMap.size() + " entries");

            List<ShoppingItem> cartItems = new ArrayList<>();
            for (Map.Entry<String, String> entry : mergedIngredients.entrySet()) {
                String ingredientName = entry.getKey();
                String quantityStr = entry.getValue();

                // 创建新的购物项
                ShoppingItem item = new ShoppingItem();
                item.name = ingredientName;
                item.selectedSkuName = ingredientName;

                // === 新增：保存Recipe需求量 ===
                item.recipeNeededStr = quantityStr; // 保存原始字符串用于显示

                // 解析需求量
                QuantityParser.ParsedQuantity parsed = QuantityParser.parse(quantityStr);
                if (parsed.success) {
                    item.recipeNeededValue = parsed.value;
                    item.recipeNeededUnit = parsed.unit;
                } else {
                    item.recipeNeededValue = 1.0;
                    item.recipeNeededUnit = "";
                }


                // 从预置数据获取价格等信息（尝试多种匹配方式）
                String normalizedName = ingredientName.toLowerCase().trim();
                ShoppingItem preset = presetMap.get(normalizedName);

// 如果直接匹配失败，尝试移除空格
                if (preset == null) {
                    preset = presetMap.get(normalizedName.replaceAll("\\s+", ""));
                }

//                android.util.Log.d("ListFragment", "Ingredient: " + ingredientName +
//                        ", Preset found: " + (preset != null ? preset.name : "NOT FOUND"));

//                android.util.Log.d("ListFragment", "Converting: " + ingredientName +
//                        " | normalizedName: " + normalizedName +
//                        " | preset found: " + (preset != null));
                if (preset != null) {
                    item.selectedSkuName = preset.selectedSkuName; // 这个字段是repo.loadInitialItems已经填充好的
                    item.unitPrice = preset.unitPrice;
                    item.unit = preset.unit;
                    item.aisle = preset.aisle;
                    item.ingredientId = preset.ingredientId;
                    item.skuSpec = preset.skuSpec;

//                    android.util.Log.d("ListFragment", "  -> Using SKU: " + item.selectedSkuName +
//                            ", Spec: " + item.skuSpec + ", Price: " + item.unitPrice);

                    // === 智能计算购买件数 ===
                    QuantityParser.ParsedQuantity packageParsed = QuantityParser.parse(preset.skuSpec);

                    if (packageParsed.success && item.recipeNeededValue > 0) {
                        // 检查单位是否匹配
                        boolean unitMatch = item.recipeNeededUnit.isEmpty() ||
                                packageParsed.unit.isEmpty() ||
                                item.recipeNeededUnit.equalsIgnoreCase(packageParsed.unit);

                        if (unitMatch) {
                            // 单位匹配，计算需要买几件
                            item.quantity = QuantityParser.calculatePackageCount(
                                    item.recipeNeededValue,
                                    packageParsed.value
                            );
                        } else {
                            // 单位不匹配，默认买1件
                            item.quantity = 1;
                            android.util.Log.w("ListFragment", "Unit mismatch: need " +
                                    item.recipeNeededUnit + " but package is " + packageParsed.unit);
                        }
                    } else {
                        // 无法解析包装规格或需求量，默认买1件
                        item.quantity = 1;
                    }

                } else {
                    // 使用默认值
                    item.unitPrice = 2.99;
                    item.unit = "Oz";
                    item.aisle = "General";
                    item.ingredientId = "cart_" + ingredientName.toLowerCase().replaceAll("[^a-z0-9]", "_");
                    item.quantity = 1;
                    item.selectedSkuName = ingredientName; // 回退到食材名
                    item.skuSpec = ""; // 无规格信息
                }

                // 优先使用Recipe模块的图片资源ID
                Integer imageResId = ingredientImageMap.get(ingredientName.toLowerCase().trim());
                if (imageResId != null && imageResId != 0) {
                    // 将资源ID转换为URL格式供Glide使用
                    item.imageUrl = "res:" + imageResId;
                } else if (preset != null && preset.imageUrl != null) {
                    // 回退到预置数据的图片
                    item.imageUrl = preset.imageUrl;
                } else {
                    // 无图片
                    item.imageUrl = "";
                }

                cartItems.add(item);
            }

            // 更新列表
            listViewModel.updateItemList(cartItems);
            android.util.Log.d("ListFragment", "Converted " + cartItems.size() + " items from cart");

        } catch (Exception e) {
            android.util.Log.e("ListFragment", "Error converting cart data: " + e.getMessage());
            e.printStackTrace();
            listViewModel.updateItemList(new ArrayList<>());
        }
    }

    /**
     * 从数量字符串解析数字 (例如: "6 Oz" -> 6.0)
     */
    private double parseQuantityFromString(String quantityStr) {
        try {
            String[] parts = quantityStr.trim().split("\\s+");
            if (parts.length > 0) {
                String numPart = parts[0].replaceAll("[^\\d.]", "");
                if (!numPart.isEmpty()) {
                    return Double.parseDouble(numPart);
                }
            }
            return 1.0; // 默认数量
        } catch (Exception e) {
            return 1.0; // 解析失败时默认为1
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // 防止内存泄漏
    }
}
