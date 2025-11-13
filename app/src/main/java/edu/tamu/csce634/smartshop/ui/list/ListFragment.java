package edu.tamu.csce634.smartshop.ui.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import edu.tamu.csce634.smartshop.Repository.ProfileRepository;
import edu.tamu.csce634.smartshop.adapters.ShoppingItemAdapter;
import edu.tamu.csce634.smartshop.databinding.FragmentListBinding;
import edu.tamu.csce634.smartshop.data.DataSeeder;
import edu.tamu.csce634.smartshop.data.PresetRepository;
import edu.tamu.csce634.smartshop.models.ShoppingItem;
import edu.tamu.csce634.smartshop.ui.recipe.RecipeViewModel;
import edu.tamu.csce634.smartshop.utils.ConflictDetector;
import edu.tamu.csce634.smartshop.utils.IngredientSubstitutes;
import edu.tamu.csce634.smartshop.utils.PreferenceStateManager;
import edu.tamu.csce634.smartshop.utils.QuantityParser;

public class ListFragment extends Fragment {

    private FragmentListBinding binding;
    private ListViewModel listViewModel;
    private RecipeViewModel recipeViewModel;
    private ShoppingItemAdapter adapter;
    private PresetRepository repo;
    private PreferenceStateManager stateManager;

    private boolean preferenceMode = false;
    private static final String PREF_MODE_KEY = "shopping_list_preference_mode";

    // ✅ 新增：标记是否需要恢复状态
    private boolean needsStateRestore = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DataSeeder.seedIfNeeded(requireContext());
        repo = new PresetRepository(requireContext());
        stateManager = new PreferenceStateManager(requireContext());

        listViewModel = new ViewModelProvider(requireActivity()).get(ListViewModel.class);
        recipeViewModel = new ViewModelProvider(requireActivity()).get(RecipeViewModel.class);
        recipeViewModel.init(requireContext());

        IngredientSubstitutes.validateSubstitutes(requireContext());

        RecyclerView recyclerView = binding.recycler;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ShoppingItemAdapter(new ArrayList<>(), listViewModel);
        adapter.setParentFragment(this);  // ✅ 设置Fragment引用
        recyclerView.setAdapter(adapter);

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

        // ✅ 关键修改：在列表更新后立即恢复状态
        listViewModel.getItemList().observe(getViewLifecycleOwner(), list -> {
            if (adapter == null) {
                adapter = new ShoppingItemAdapter(list, listViewModel);
                adapter.setParentFragment(this);  // ✅ 设置Fragment引用
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateData(list);
            }

            // ✅ 如果需要恢复状态且列表已准备好
            if (needsStateRestore && list != null && !list.isEmpty()) {
                needsStateRestore = false;
                binding.getRoot().post(() -> restorePreferenceModeState());
            }
        });

        android.content.SharedPreferences modePref = requireContext()
                .getSharedPreferences("SmartShopListPrefs", android.content.Context.MODE_PRIVATE);
        preferenceMode = modePref.getBoolean(PREF_MODE_KEY, false);
        updateModeUI();

        // ✅ 如果是偏好模式，标记需要恢复状态
        if (preferenceMode) {
            needsStateRestore = true;
        }

        binding.chipModeToggle.setOnClickListener(v -> togglePreferenceMode());

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

        binding.btnProceed.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Navigating to Store Map… (demo)", Toast.LENGTH_SHORT).show());
    }

    private void convertCartToShoppingList(Map<String, String> mergedIngredients) {
        try {
            List<edu.tamu.csce634.smartshop.models.Recipe> recipes = recipeViewModel.getRecipes().getValue();
            if (recipes == null) recipes = new ArrayList<>();

            Map<String, Integer> ingredientImageMap = new HashMap<>();
            for (edu.tamu.csce634.smartshop.models.Recipe recipe : recipes) {
                for (edu.tamu.csce634.smartshop.models.Ingredient ingredient : recipe.getIngredients()) {
                    ingredientImageMap.put(ingredient.getName().toLowerCase().trim(), ingredient.getImageResId());
                }
            }

            List<ShoppingItem> presetItems = repo.loadInitialItems();
            Map<String, ShoppingItem> presetMap = new HashMap<>();
            for (ShoppingItem preset : presetItems) {
                String normalizedName = preset.name.toLowerCase().trim();
                presetMap.put(normalizedName, preset);
                presetMap.put(normalizedName.replaceAll("\\s+", ""), preset);
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

                ShoppingItem item = new ShoppingItem();
                item.name = ingredientName;
                item.selectedSkuName = ingredientName;
                item.recipeNeededStr = quantityStr;

                QuantityParser.ParsedQuantity parsed = QuantityParser.parse(quantityStr);
                if (parsed.success) {
                    item.recipeNeededValue = parsed.value;
                    item.recipeNeededUnit = parsed.unit;
                } else {
                    item.recipeNeededValue = 1.0;
                    item.recipeNeededUnit = "";
                }

                String normalizedName = ingredientName.toLowerCase().trim();
                ShoppingItem preset = presetMap.get(normalizedName);
                if (preset == null) preset = presetMap.get(normalizedName.replaceAll("\\s+", ""));

                if (preset != null) {
                    item.selectedSkuName = preset.selectedSkuName;
                    item.unitPrice = preset.unitPrice;
                    item.unit = preset.unit;
                    item.aisle = preset.aisle;
                    item.ingredientId = preset.ingredientId;
                    item.skuSpec = preset.skuSpec;

                    QuantityParser.ParsedQuantity packageParsed = QuantityParser.parse(preset.skuSpec);
                    if (packageParsed.success && item.recipeNeededValue > 0) {
                        boolean unitMatch = item.recipeNeededUnit.isEmpty() || packageParsed.unit.isEmpty() ||
                                item.recipeNeededUnit.equalsIgnoreCase(packageParsed.unit);
                        if (unitMatch) {
                            item.quantity = QuantityParser.calculatePackageCount(item.recipeNeededValue, packageParsed.value);
                        } else {
                            item.quantity = 1;
                        }
                    } else {
                        item.quantity = 1;
                    }
                } else {
                    item.unitPrice = 2.99;
                    item.unit = "Oz";
                    item.aisle = "General";
                    item.ingredientId = "cart_" + ingredientName.toLowerCase().replaceAll("[^a-z0-9]", "_");
                    item.quantity = 1;
                    item.selectedSkuName = ingredientName;
                    item.skuSpec = "";
                }

                Integer imageResId = ingredientImageMap.get(ingredientName.toLowerCase().trim());
                if (imageResId != null && imageResId != 0) {
                    item.imageUrl = "res:" + imageResId;
                } else if (preset != null && preset.imageUrl != null) {
                    item.imageUrl = preset.imageUrl;
                } else {
                    item.imageUrl = "";
                }

                cartItems.add(item);
            }

            listViewModel.updateItemList(cartItems);
            // ✅ 关键修复：如果是偏好模式，立即应用历史替换
            if (preferenceMode) {
                binding.getRoot().post(() -> {
                    applyHistoricalSubstitutions(cartItems);
                    listViewModel.updateItemList(cartItems);
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            listViewModel.updateItemList(new ArrayList<>());
        }
    }

    private void togglePreferenceMode() {
        if (!preferenceMode) {
            saveCurrentQuantities(false);
            enablePreferenceMode();
        } else {
            saveCurrentQuantities(true);
            disablePreferenceMode();
        }
    }

    private void saveCurrentQuantities(boolean isPreferenceMode) {
        List<ShoppingItem> currentItems = listViewModel.getItemList().getValue();
        if (currentItems == null) return;

        Map<String, Integer> quantities = new HashMap<>();
        for (ShoppingItem item : currentItems) {
            String key = item.originalIngredientId != null ? item.originalIngredientId : item.ingredientId;
            quantities.put(key, (int) Math.round(item.quantity));
        }

        if (isPreferenceMode) {
            stateManager.savePreferenceQuantities(quantities);
        } else {
            stateManager.saveDefaultQuantities(quantities);
        }
    }

    private void enablePreferenceMode() {
        ProfileRepository profileRepo = new ProfileRepository(requireActivity().getApplication());
        profileRepo.getProfileData().observe(getViewLifecycleOwner(), profile -> {
            if (profile == null) {
                Toast.makeText(requireContext(), "Please set up your profile first", Toast.LENGTH_SHORT).show();
                profileRepo.getProfileData().removeObservers(getViewLifecycleOwner());
                return;
            }

            List<ShoppingItem> currentItems = listViewModel.getItemList().getValue();
            if (currentItems == null || currentItems.isEmpty()) {
                Toast.makeText(requireContext(), "Shopping list is empty", Toast.LENGTH_SHORT).show();
                profileRepo.getProfileData().removeObservers(getViewLifecycleOwner());
                return;
            }

            // ✅ 读取历史替换记录
            Map<String, PreferenceStateManager.ResolutionRecord> savedResolutions = stateManager.loadResolutions();

            if (!savedResolutions.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Restoring " + savedResolutions.size() + " previous changes...",
                        Toast.LENGTH_SHORT).show();

                List<ShoppingItem> restoredItems = new ArrayList<>();
                int appliedCount = 0;

                for (ShoppingItem item : currentItems) {
                    ShoppingItem newItem = new ShoppingItem();
                    copyItemFields(item, newItem);

                    // ✅ 关键修复：尝试多种key匹配
                    PreferenceStateManager.ResolutionRecord resolution = null;

                    // 方式1：直接匹配ingredientId
                    resolution = savedResolutions.get(item.ingredientId);

                    // 方式2：如果有originalIngredientId，用它匹配
                    if (resolution == null && item.originalIngredientId != null) {
                        resolution = savedResolutions.get(item.originalIngredientId);
                    }

                    // 方式3：按名称模糊匹配（最后手段）
                    if (resolution == null) {
                        for (Map.Entry<String, PreferenceStateManager.ResolutionRecord> entry : savedResolutions.entrySet()) {
                            if (item.name.equalsIgnoreCase(entry.getValue().substituteName)) {
                                resolution = entry.getValue();
                                Toast.makeText(requireContext(),
                                        "DEBUG: Matched by name: " + item.name,
                                        Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                    }

                    if (resolution != null) {
                        Toast.makeText(requireContext(),
                                "DEBUG: Found resolution for " + item.name,
                                Toast.LENGTH_SHORT).show();

                        if (resolution.type == PreferenceStateManager.ResolutionType.REPLACED) {
                            applySubstitutionToItem(newItem, resolution);
                        } else if (resolution.type == PreferenceStateManager.ResolutionType.SET_TO_ZERO) {
                            newItem.quantity = 0;
                        }
                    } else {
                        Toast.makeText(requireContext(),
                                "DEBUG: No resolution found for " + item.ingredientId + "/" + item.name,
                                Toast.LENGTH_SHORT).show();
                    }

                    restoredItems.add(newItem);
                }
                // ✅ 强制更新列表
                listViewModel.updateItemList(restoredItems);
                currentItems = restoredItems;

                Toast.makeText(requireContext(),
                        "✓ Applied " + appliedCount + " substitutions",
                        Toast.LENGTH_LONG).show();
                // ✅ 调试：打印保存的记录
                for (Map.Entry<String, PreferenceStateManager.ResolutionRecord> entry : savedResolutions.entrySet()) {
                    Toast.makeText(requireContext(),
                            "Saved: [" + entry.getKey() + "] → " + entry.getValue().substituteName,
                            Toast.LENGTH_LONG).show();
                }

// ✅ 调试：打印当前列表
                for (ShoppingItem item : currentItems) {
                    Toast.makeText(requireContext(),
                            "Current: [" + item.ingredientId + "] " + item.name,
                            Toast.LENGTH_LONG).show();
                }
            }

            // 检测冲突
            List<ConflictDetector.Conflict> conflicts = ConflictDetector.detectConflicts(currentItems, profile);

            // 标记已解决的冲突
            for (ConflictDetector.Conflict conflict : conflicts) {
                String key = conflict.item.originalIngredientId != null ?
                        conflict.item.originalIngredientId : conflict.item.ingredientId;
                if (savedResolutions.containsKey(key)) {
                    conflict.resolved = true;
                }
            }

            if (!conflicts.isEmpty()) {
                adapter.setConflicts(conflicts);
                adapter.setOnSubstituteRequestListener(this::showSubstituteBottomSheet);

                int unresolvedCount = 0;
                for (ConflictDetector.Conflict c : conflicts) {
                    if (!c.resolved) unresolvedCount++;
                }

                if (unresolvedCount > 0) {
                    Toast.makeText(requireContext(),
                            unresolvedCount + " conflicts detected. Tap items to resolve.",
                            Toast.LENGTH_LONG).show();
                }
            }

            activatePreferenceMode(profile);
            restorePreferenceQuantities();
            profileRepo.getProfileData().removeObservers(getViewLifecycleOwner());
        });
    }
    private void activatePreferenceMode(edu.tamu.csce634.smartshop.models.ProfileData profile) {
        preferenceMode = true;
        updateModeUI();
        savePreferenceMode();
        Toast.makeText(requireContext(), "Preference mode activated", Toast.LENGTH_SHORT).show();
    }

    private void disablePreferenceMode() {
        preferenceMode = false;
        updateModeUI();
        savePreferenceMode();
        restoreDefaultMode();
        if (adapter != null) adapter.setConflicts(new ArrayList<>());
        Toast.makeText(requireContext(), "✓ Switched to default mode", Toast.LENGTH_SHORT).show();
    }

    private void restoreDefaultMode() {
        List<ShoppingItem> currentItems = listViewModel.getItemList().getValue();
        if (currentItems == null) return;

        Map<String, Integer> savedQuantities = stateManager.loadDefaultQuantities();
        List<ShoppingItem> restoredItems = new ArrayList<>();

        for (ShoppingItem item : currentItems) {
            ShoppingItem newItem = new ShoppingItem();
            copyItemFields(item, newItem);

            if (item.isSubstituted && item.originalIngredientId != null) {
                try {
                    ShoppingItem originalPreset = loadPresetDataForSubstitute(item.originalIngredientId);
                    if (originalPreset != null) {
                        newItem.ingredientId = item.originalIngredientId;
                        newItem.name = originalPreset.name;
                        newItem.selectedSkuName = originalPreset.selectedSkuName;
                        newItem.unitPrice = originalPreset.unitPrice;
                        newItem.skuSpec = originalPreset.skuSpec;
                        newItem.unit = originalPreset.unit;
                        newItem.aisle = originalPreset.aisle;
                        newItem.imageUrl = getImageUrlForIngredient(originalPreset.name);
                        newItem.isSubstituted = false;
                        newItem.substitutionRatio = 1.0;
                        newItem.originalIngredientId = null;

                        // ✅ 恢复原始Recipe需求量
                        newItem.recipeNeededValue = item.recipeNeededValue / item.substitutionRatio;
                        newItem.recipeNeededStr = formatQuantity(newItem.recipeNeededValue) +
                                (newItem.recipeNeededUnit.isEmpty() ? "" : " " + newItem.recipeNeededUnit);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String key = item.originalIngredientId != null ? item.originalIngredientId : item.ingredientId;
            Integer savedQty = savedQuantities.get(key);
            if (savedQty != null) {
                newItem.quantity = savedQty;
            }

            restoredItems.add(newItem);
        }

        listViewModel.updateItemList(restoredItems);
    }

    private void restorePreferenceQuantities() {
        Map<String, Integer> savedQuantities = stateManager.loadPreferenceQuantities();
        if (savedQuantities.isEmpty()) return;

        List<ShoppingItem> currentItems = listViewModel.getItemList().getValue();
        if (currentItems == null) return;

        for (ShoppingItem item : currentItems) {
            String key = item.originalIngredientId != null ? item.originalIngredientId : item.ingredientId;
            Integer savedQty = savedQuantities.get(key);
            if (savedQty != null) {
                item.quantity = savedQty;
            }
        }

        listViewModel.updateItemList(currentItems);
    }

    private void restorePreferenceModeState() {
        List<ShoppingItem> currentItems = listViewModel.getItemList().getValue();
        if (currentItems == null || currentItems.isEmpty()) return;

        ProfileRepository profileRepo = new ProfileRepository(requireActivity().getApplication());
        profileRepo.getProfileData().observe(getViewLifecycleOwner(), profile -> {
            if (profile == null) {
                profileRepo.getProfileData().removeObservers(getViewLifecycleOwner());
                return;
            }

            applyHistoricalSubstitutions(currentItems);
            listViewModel.updateItemList(currentItems);

            List<ConflictDetector.Conflict> conflicts = ConflictDetector.detectConflicts(currentItems, profile);
            Map<String, PreferenceStateManager.ResolutionRecord> savedResolutions = stateManager.loadResolutions();

            for (ConflictDetector.Conflict conflict : conflicts) {
                String key = conflict.item.originalIngredientId != null ?
                        conflict.item.originalIngredientId : conflict.item.ingredientId;
                if (savedResolutions.containsKey(key)) {
                    conflict.resolved = true;
                }
            }

            if (!conflicts.isEmpty() || !savedResolutions.isEmpty()) {
                adapter.setConflicts(conflicts);
                adapter.setOnSubstituteRequestListener(this::showSubstituteBottomSheet);
            }

            profileRepo.getProfileData().removeObservers(getViewLifecycleOwner());
        });
    }

    // ✅ 关键方法：应用历史替换
    private void applyHistoricalSubstitutions(List<ShoppingItem> items) {
        Map<String, PreferenceStateManager.ResolutionRecord> savedResolutions = stateManager.loadResolutions();

        for (ShoppingItem item : items) {
            PreferenceStateManager.ResolutionRecord resolution = savedResolutions.get(item.ingredientId);

            if (resolution != null && resolution.type == PreferenceStateManager.ResolutionType.REPLACED) {
                applySubstitutionToItem(item, resolution);
            }
        }
    }

    private void applySubstitutionToItem(ShoppingItem item, PreferenceStateManager.ResolutionRecord resolution) {
        try {
            ShoppingItem presetData = loadPresetDataForSubstitute(resolution.substituteId);
            if (presetData == null) {
                Toast.makeText(requireContext(),
                        "⚠ Cannot load preset for " + resolution.substituteName,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int substituteImageResId = getImageResIdForIngredient(resolution.substituteName);
            presetData.imageUrl = "res:" + substituteImageResId;

            // ✅ 保存原始ID
            if (item.originalIngredientId == null) {
                item.originalIngredientId = item.ingredientId;
            }

            double originalNeededValue = item.recipeNeededValue;

            // ✅ 更新所有字段
            item.ingredientId = resolution.substituteId;
            item.name = resolution.substituteName;
            item.selectedSkuName = presetData.selectedSkuName;
            item.unitPrice = presetData.unitPrice;
            item.skuSpec = presetData.skuSpec;
            item.unit = presetData.unit;
            item.aisle = presetData.aisle;
            item.imageUrl = presetData.imageUrl;
            item.isSubstituted = true;
            item.substitutionRatio = resolution.substitutionRatio;
            item.substituteDisplayName = "Replaced with " + resolution.substituteName;

            item.recipeNeededValue = originalNeededValue * resolution.substitutionRatio;
            item.recipeNeededStr = formatQuantity(item.recipeNeededValue) +
                    (item.recipeNeededUnit.isEmpty() ? "" : " " + item.recipeNeededUnit);

            QuantityParser.ParsedQuantity packageParsed = QuantityParser.parse(item.skuSpec);
            if (packageParsed.success && item.recipeNeededValue > 0) {
                boolean unitMatch = item.recipeNeededUnit.isEmpty() || packageParsed.unit.isEmpty() ||
                        item.recipeNeededUnit.equalsIgnoreCase(packageParsed.unit);
                if (unitMatch) {
                    item.quantity = QuantityParser.calculatePackageCount(item.recipeNeededValue, packageParsed.value);
                } else {
                    item.quantity = 1;
                }
            }

            // ✅ 添加成功提示
            Toast.makeText(requireContext(),
                    "✓ Applied: " + item.name + " (ID: " + item.ingredientId + ")",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    "⚠ Error applying substitution: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private void showSubstituteBottomSheet(ShoppingItem item, ConflictDetector.Conflict conflict) {
        SubstituteSelectionBottomSheet sheet = SubstituteSelectionBottomSheet.newInstance(item, conflict);
        sheet.setOnSubstituteConfirmedListener((originalItem, selectedSubstitute) -> {
            try {
                ShoppingItem presetData = loadPresetDataForSubstitute(selectedSubstitute.ingredientId);
                if (presetData == null) {
                    Toast.makeText(requireContext(), "⚠ Failed to load substitute data", Toast.LENGTH_SHORT).show();
                    return;
                }

                presetData.imageUrl = "res:" + selectedSubstitute.imageResId;
                double ratio = parseQuantityRatio(selectedSubstitute.quantityAdjustment);

                boolean success = listViewModel.replaceIngredient(
                        originalItem.ingredientId,
                        selectedSubstitute.ingredientId,
                        selectedSubstitute.name,
                        ratio,
                        presetData
                );

                if (success) {
                    conflict.resolved = true;
                    // ✅ 关键修复：使用originalIngredientId（如果有的话）
                    String keyToSave = originalItem.originalIngredientId != null ?
                            originalItem.originalIngredientId : originalItem.ingredientId;

                    stateManager.recordResolution(
                            keyToSave,  // ✅ 使用正确的key
                            PreferenceStateManager.ResolutionType.REPLACED,
                            selectedSubstitute.ingredientId,
                            selectedSubstitute.name,
                            (int) Math.round(originalItem.quantity),
                            ratio
                    );
                    Toast.makeText(requireContext(),
                            "DEBUG: Saved resolution for key=" + keyToSave,
                            Toast.LENGTH_SHORT).show();

                    adapter.setConflicts(adapter.getConflicts());
                    int newQty = getCurrentQuantityForItem(selectedSubstitute.ingredientId);
                    Toast.makeText(requireContext(),
                            String.format("✓ Replaced %s with %s\nNew quantity: %d",
                                    originalItem.name, selectedSubstitute.name, newQty),
                            Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(requireContext(), "⚠ Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        sheet.show(getParentFragmentManager(), "SubstituteSelectionBottomSheet");
    }

    private ShoppingItem loadPresetDataForSubstitute(String substituteIngredientId) {
        try {
            for (ShoppingItem preset : repo.loadInitialItems()) {
                if (substituteIngredientId.equals(preset.ingredientId)) {
                    return preset;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private double parseQuantityRatio(String quantityAdjustment) {
        if (quantityAdjustment == null || quantityAdjustment.isEmpty()) return 1.0;
        try {
            if (quantityAdjustment.toLowerCase().contains("1:1")) return 1.0;
            if (quantityAdjustment.contains("→")) {
                String[] parts = quantityAdjustment.split("→");
                if (parts.length == 2) {
                    double original = extractNumber(parts[0].trim());
                    double substitute = extractNumber(parts[1].trim());
                    if (original > 0) return substitute / original;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1.0;
    }

    private double extractNumber(String text) {
        try {
            for (String token : text.split("\\s+")) {
                String cleaned = token.replaceAll("[^0-9.]", "");
                if (!cleaned.isEmpty()) return Double.parseDouble(cleaned);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1.0;
    }

    private int getCurrentQuantityForItem(String ingredientId) {
        List<ShoppingItem> currentList = listViewModel.getItemList().getValue();
        if (currentList != null) {
            for (ShoppingItem item : currentList) {
                if (ingredientId.equals(item.ingredientId)) {
                    return (int) Math.round(item.quantity);
                }
            }
        }
        return 0;
    }

    private int getImageResIdForIngredient(String ingredientName) {
        for (IngredientSubstitutes.Substitute sub : getAllPossibleSubstitutes()) {
            if (sub.name.equalsIgnoreCase(ingredientName)) {
                return sub.imageResId;
            }
        }
        return R.drawable.tofu;
    }

    private List<IngredientSubstitutes.Substitute> getAllPossibleSubstitutes() {
        List<IngredientSubstitutes.Substitute> allSubs = new ArrayList<>();
        String[] testIngredients = {"Egg", "Steak", "Salmon", "Queso Fresco", "Flour Tortilla"};
        for (String ing : testIngredients) {
            allSubs.addAll(IngredientSubstitutes.getVeganSubstitutes(ing));
            allSubs.addAll(IngredientSubstitutes.getVegetarianSubstitutes(ing));
            allSubs.addAll(IngredientSubstitutes.getGlutenFreeSubstitutes(ing));
        }
        return allSubs;
    }

    private String getImageUrlForIngredient(String ingredientName) {
        try {
            List<edu.tamu.csce634.smartshop.models.Recipe> recipes = recipeViewModel.getRecipes().getValue();
            if (recipes != null) {
                for (edu.tamu.csce634.smartshop.models.Recipe recipe : recipes) {
                    for (edu.tamu.csce634.smartshop.models.Ingredient ingredient : recipe.getIngredients()) {
                        if (ingredient.getName().equalsIgnoreCase(ingredientName)) {
                            return "res:" + ingredient.getImageResId();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "res:" + R.drawable.tofu;
    }

    private void copyItemFields(ShoppingItem from, ShoppingItem to) {
        to.ingredientId = from.ingredientId;
        to.name = from.name;
        to.unit = from.unit;
        to.quantity = from.quantity;
        to.aisle = from.aisle;
        to.unitPrice = from.unitPrice;
        to.selectedSkuName = from.selectedSkuName;
        to.skuSpec = from.skuSpec;
        to.imageUrl = from.imageUrl;
        to.recipeNeededStr = from.recipeNeededStr;
        to.recipeNeededValue = from.recipeNeededValue;
        to.recipeNeededUnit = from.recipeNeededUnit;
        to.originalIngredientId = from.originalIngredientId;
        to.substitutionRatio = from.substitutionRatio;
        to.isSubstituted = from.isSubstituted;
        to.substituteDisplayName = from.substituteDisplayName;
    }

    private String formatQuantity(double value) {
        if (Math.abs(value - Math.round(value)) < 1e-9) {
            return String.valueOf((int) Math.round(value));
        }
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private void updateModeUI() {
        if (preferenceMode) {
            binding.chipModeToggle.setChecked(true);
            binding.chipModeToggle.setText("Preference");
            binding.chipModeToggle.setTextColor(0xFFFFFFFF);
            binding.chipModeToggle.setChipBackgroundColorResource(R.color.green_700);
        } else {
            binding.chipModeToggle.setChecked(false);
            binding.chipModeToggle.setText("Default");
            binding.chipModeToggle.setTextColor(0xFF388E3C);
            binding.chipModeToggle.setChipBackgroundColorResource(android.R.color.white);
        }
    }

    private void savePreferenceMode() {
        requireContext().getSharedPreferences("SmartShopListPrefs", android.content.Context.MODE_PRIVATE)
                .edit().putBoolean(PREF_MODE_KEY, preferenceMode).apply();
    }
    /**
     * 记录通过-按钮解决的冲突
     */
    public void recordQuantityZeroResolution(String ingredientId) {
        stateManager.recordResolution(
                ingredientId,
                PreferenceStateManager.ResolutionType.SET_TO_ZERO,
                null,
                null,
                0,
                1.0
        );
    }

    /**
     * 清除通过+按钮重新激活的冲突记录
     */
    public void clearResolutionForReactivatedConflict(String ingredientId) {
        Map<String, PreferenceStateManager.ResolutionRecord> resolutions = stateManager.loadResolutions();
        if (resolutions.containsKey(ingredientId)) {
            resolutions.remove(ingredientId);

            // 重新保存剩余记录
            android.content.SharedPreferences prefs = requireContext()
                    .getSharedPreferences("SmartShopPreferenceState", android.content.Context.MODE_PRIVATE);
            prefs.edit().putString("resolution_actions", new com.google.gson.Gson().toJson(resolutions)).apply();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}