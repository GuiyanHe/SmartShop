package edu.tamu.csce634.smartshop.ui.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.adapters.ShoppingItemAdapter;
import edu.tamu.csce634.smartshop.databinding.FragmentListBinding;
import edu.tamu.csce634.smartshop.data.DataSeeder;
import edu.tamu.csce634.smartshop.data.PresetRepository;
import edu.tamu.csce634.smartshop.models.ShoppingItem;
import edu.tamu.csce634.smartshop.ui.recipe.RecipeViewModel;
import edu.tamu.csce634.smartshop.utils.ConflictDetector;
import edu.tamu.csce634.smartshop.utils.IngredientSubstitutes;
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

    // 新增：偏好模式状态
    private boolean preferenceMode = false;
    private static final String PREF_MODE_KEY = "shopping_list_preference_mode";

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

        // ✅ Phase 1: 验证替代品数据
        IngredientSubstitutes.validateSubstitutes(requireContext());

        // 3) RecyclerView 基本配置
        RecyclerView recyclerView = binding.recycler;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ShoppingItemAdapter(new ArrayList<>(), listViewModel);
        recyclerView.setAdapter(adapter);

        // 4) 绑定总价显示（分为三个TextView）
        listViewModel.getTotal().observe(getViewLifecycleOwner(), total -> {
            int itemCount = listViewModel.getItemList().getValue() != null ?
                    listViewModel.getItemList().getValue().size() : 0;
            binding.totalPrice.setText(String.format(java.util.Locale.US, "$%.2f", total));
            if (itemCount > 0) {
                binding.itemCount.setText(String.format("(%d items)", itemCount));
            } else {
                binding.itemCount.setText("(0 items)");
            }
        });

        // 5) 观察列表变化
        listViewModel.getItemList().observe(getViewLifecycleOwner(), list -> {
            if (adapter == null) {
                adapter = new ShoppingItemAdapter(list, listViewModel);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateData(list);
            }
        });

        // 6) 加载并恢复模式状态
        android.content.SharedPreferences modePref = requireContext()
                .getSharedPreferences("SmartShopListPrefs", android.content.Context.MODE_PRIVATE);
        preferenceMode = modePref.getBoolean(PREF_MODE_KEY, false);
        updateModeUI();

        // 7) 模式切换点击事件
        binding.chipModeToggle.setOnClickListener(v -> {
            togglePreferenceMode();
        });

        // 8) 观察购物车聚合食材数据
        recipeViewModel.getRequiredIngredients().observe(getViewLifecycleOwner(), merged -> {
            if (merged != null && !merged.isEmpty()) {
                binding.emptyStateLayout.setVisibility(View.GONE);
                binding.recycler.setVisibility(View.VISIBLE);
                convertCartToShoppingList(merged);
            } else {
                binding.emptyStateLayout.setVisibility(View.VISIBLE);
                binding.recycler.setVisibility(View.GONE);
                listViewModel.updateItemList(new ArrayList<>());
            }
        });

        // 9) Continue按钮点击事件
        binding.btnProceed.setOnClickListener(v -> {
            android.widget.Toast.makeText(requireContext(),
                    "Navigating to Store Map… (demo)",
                    android.widget.Toast.LENGTH_SHORT).show();
        });
    }

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

                if (preset != null) {
                    item.selectedSkuName = preset.selectedSkuName;
                    item.unitPrice = preset.unitPrice;
                    item.unit = preset.unit;
                    item.aisle = preset.aisle;
                    item.ingredientId = preset.ingredientId;
                    item.skuSpec = preset.skuSpec;

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
                    item.selectedSkuName = ingredientName;
                    item.skuSpec = "";
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
     * 切换偏好模式
     */
    private void togglePreferenceMode() {
        if (!preferenceMode) {
            // 尝试启用偏好模式
            enablePreferenceMode();
        } else {
            // 禁用偏好模式
            disablePreferenceMode();
        }
    }

    /**
     * 启用偏好模式
     */
    private void enablePreferenceMode() {
        android.util.Log.d("ListFragment", "=== enablePreferenceMode called ===");

        // 1. 获取用户Profile（使用observe确保数据已加载）
        edu.tamu.csce634.smartshop.Repository.ProfileRepository profileRepo =
                new edu.tamu.csce634.smartshop.Repository.ProfileRepository(
                        requireActivity().getApplication());

        profileRepo.getProfileData().observe(getViewLifecycleOwner(), profile -> {
            if (profile == null) {
                android.util.Log.d("ListFragment", "Profile is null");
                android.widget.Toast.makeText(requireContext(),
                        "Please set up your profile first",
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            android.util.Log.d("ListFragment", "Profile loaded: " + profile.getName());
            android.util.Log.d("ListFragment", "  isVegan: " + profile.isVegan());
            android.util.Log.d("ListFragment", "  isVegetarian: " + profile.isVegetarian());
            android.util.Log.d("ListFragment", "  Allergies: " + profile.getAllergies());

            // 2. 获取当前购物清单
            List<edu.tamu.csce634.smartshop.models.ShoppingItem> currentItems =
                    listViewModel.getItemList().getValue();

            if (currentItems == null || currentItems.isEmpty()) {
                android.util.Log.d("ListFragment", "Shopping list is empty");
                android.widget.Toast.makeText(requireContext(),
                        "Shopping list is empty",
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            android.util.Log.d("ListFragment", "Shopping list has " + currentItems.size() + " items");
            for (edu.tamu.csce634.smartshop.models.ShoppingItem item : currentItems) {
                android.util.Log.d("ListFragment", "  - " + item.name);
            }

            // 3. 检测冲突
            List<ConflictDetector.Conflict> conflicts =
                    ConflictDetector.detectConflicts(currentItems, profile);

            android.util.Log.d("ListFragment", "Detected " + conflicts.size() + " conflicts");

            if (!conflicts.isEmpty()) {
                // ✅ Phase 2: 传递冲突数据到Adapter
                adapter.setConflicts(conflicts);

                // ✅ 设置替代品选择监听器
                adapter.setOnSubstituteRequestListener((item, conflict) -> {
                    showSubstituteBottomSheet(item, conflict);
                });

                // 显示冲突信息
                StringBuilder sb = new StringBuilder("Found conflicts:\n");
                for (ConflictDetector.Conflict c : conflicts) {
                    sb.append("- ").append(c.item.name).append(": ").append(c.reason).append("\n");
                    android.util.Log.d("ListFragment", "  Conflict: " + c.item.name + " - " + c.reason);

                    // 调试：输出替代品信息
                    if (c.substitutes != null && !c.substitutes.isEmpty()) {
                        android.util.Log.d("ListFragment", "    Available substitutes: " + c.substitutes.size());
                        for (IngredientSubstitutes.Substitute sub : c.substitutes) {
                            android.util.Log.d("ListFragment", "      - " + sub.name + " (" + sub.ingredientId + ")");
                        }
                    } else {
                        android.util.Log.w("ListFragment", "    No substitutes available!");
                    }
                }

                android.widget.Toast.makeText(requireContext(),
                        conflicts.size() + " conflicts detected. Tap items to resolve.",
                        android.widget.Toast.LENGTH_LONG).show();

                // 激活偏好模式
                activatePreferenceMode(profile);

            } else {
                // 无冲突 - 直接启用偏好模式
                android.util.Log.d("ListFragment", "No conflicts, activating preference mode");
                activatePreferenceMode(profile);
            }

            // ⚠️ 重要：observe只触发一次，需要手动移除观察
            profileRepo.getProfileData().removeObservers(getViewLifecycleOwner());
        });
    }

    /**
     * 激活偏好模式（无冲突时）
     */
    private void activatePreferenceMode(edu.tamu.csce634.smartshop.models.ProfileData profile) {
        preferenceMode = true;
        updateModeUI();
        savePreferenceMode();

        android.widget.Toast.makeText(requireContext(),
                "Preference mode activated",
                android.widget.Toast.LENGTH_SHORT).show();
        android.util.Log.d("ListFragment", "Preference mode activated");

        // TODO: Phase 5 - 下一步应用偏好过滤和排序
    }

    /**
     * 禁用偏好模式
     */
    private void disablePreferenceMode() {
        preferenceMode = false;
        updateModeUI();
        savePreferenceMode();

        // ✅ Phase 2: 清除冲突显示
        if (adapter != null) {
            adapter.setConflicts(new ArrayList<>());
        }

        android.widget.Toast.makeText(requireContext(),
                "Switched to default mode",
                android.widget.Toast.LENGTH_SHORT).show();
        android.util.Log.d("ListFragment", "Preference mode disabled");
    }

    /**
     * 更新模式UI显示
     */
    private void updateModeUI() {
        if (preferenceMode) {
            // 偏好模式：绿色背景，白色文字，显示勾选
            binding.chipModeToggle.setChecked(true);
            binding.chipModeToggle.setText("Preference");
            binding.chipModeToggle.setTextColor(0xFFFFFFFF); // 白色
            binding.chipModeToggle.setChipBackgroundColorResource(R.color.green_700);
        } else {
            // 默认模式：白色背景，绿色文字
            binding.chipModeToggle.setChecked(false);
            binding.chipModeToggle.setText("Default");
            binding.chipModeToggle.setTextColor(0xFF388E3C); // 深绿色
            binding.chipModeToggle.setChipBackgroundColorResource(android.R.color.white);
        }
    }

    /**
     * 保存模式状态到SharedPreferences
     */
    private void savePreferenceMode() {
        requireContext().getSharedPreferences("SmartShopListPrefs", android.content.Context.MODE_PRIVATE)
                .edit()
                .putBoolean(PREF_MODE_KEY, preferenceMode)
                .apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // 防止内存泄漏
    }

    /**
     * 显示替代品选择BottomSheet
     */
    private void showSubstituteBottomSheet(ShoppingItem item, ConflictDetector.Conflict conflict) {
        SubstituteSelectionBottomSheet sheet =
                SubstituteSelectionBottomSheet.newInstance(item, conflict);

        // ✅ 设置确认回调
        sheet.setOnSubstituteConfirmedListener((originalItem, selectedSubstitute) -> {
            // TODO: Phase 4 实现实际替换逻辑

            // ✅ 标记冲突为已解决
            conflict.resolved = true;

            // ✅ 刷新 Adapter 显示（徽章会变灰）
            adapter.notifyDataSetChanged();

            android.widget.Toast.makeText(requireContext(),
                    "✓ Replaced " + originalItem.name + " with " + selectedSubstitute.name +
                            "\n(Full replacement logic in Phase 4)",
                    android.widget.Toast.LENGTH_LONG).show();
        });

        sheet.show(getParentFragmentManager(), "SubstituteSelectionBottomSheet");
    }
}