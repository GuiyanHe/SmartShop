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

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.tamu.csce634.smartshop.databinding.FragmentListBinding;
import edu.tamu.csce634.smartshop.data.DataSeeder;
import edu.tamu.csce634.smartshop.data.PresetRepository;

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

        // 3) RecyclerView 基本配置
        RecyclerView recyclerView = binding.recycler;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        // 先给空数据，后续通过 LiveData 驱动
        adapter = new ShoppingItemAdapter(new ArrayList<>(), listViewModel);
        recyclerView.setAdapter(adapter);

        // 4) 绑定总价显示（底部卡片 + 顶部右侧）
        TextView totalText = binding.totalText;  // 底部白卡里的总价文字
        listViewModel.getTotal().observe(getViewLifecycleOwner(), t -> {
            String priceText = String.format("Estimated Total: ¥%.2f", t);
            totalText.setText(priceText);
        });

        // 5) 观察列表变化，刷新适配器
        listViewModel.getItemList().observe(getViewLifecycleOwner(), list -> {
            adapter = new ShoppingItemAdapter(list, listViewModel);
            recyclerView.setAdapter(adapter);
        });

        // 6) 默认加载：Breakfast 配方
//        loadRecipe(RECIPE_BREAKFAST);
        listViewModel.updateItemList(new ArrayList<>());

        // 7) 配方切换按钮
//        binding.btnRecipeBreakfast.setOnClickListener(v -> loadRecipe(RECIPE_BREAKFAST));
//        binding.btnRecipeDinner.setOnClickListener(v -> loadRecipe(RECIPE_FAMILY_DINNER));
//        binding.btnRecipeVegan.setOnClickListener(v -> loadRecipe(RECIPE_VEGAN));

        // 8) 底部“Proceed to Map”（示例：给个反馈）
        binding.btnProceed.setOnClickListener(v ->
                totalText.setText("Navigating to Store Map… (demo)")
        );


    }

    /** 按配方筛选 items 并交给 ViewModel（会自动重算总价并刷新 UI） */
    private void loadRecipe(List<String> ingredientIds) {
        try {
            List<ShoppingItem> all = repo.loadInitialItems();  // 带默认 option
            List<ShoppingItem> filtered = new ArrayList<>();
            for (ShoppingItem it : all) {
                if (ingredientIds.contains(it.ingredientId)) {
                    filtered.add(it);
                }
            }
            listViewModel.updateItemList(filtered);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // 防止内存泄漏
    }
}
