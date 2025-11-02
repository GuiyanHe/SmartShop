package edu.tamu.csce634.smartshop.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.csce634.smartshop.R;

public class MapFragment extends Fragment {

    private ImageView imgMap;
    private TextView tvItemName;
    private TextView tvInstruction;
    private TextView tvProgress;
    private Button btnMarkDone;
    private Button btnNext;

    private final List<ShoppingItem> shoppingList = new ArrayList<>();
    private int currentIndex = 0;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgMap = view.findViewById(R.id.img_map);
        tvItemName = view.findViewById(R.id.tv_item_name);
        tvInstruction = view.findViewById(R.id.tv_instruction);
        tvProgress = view.findViewById(R.id.tv_progress);
        btnMarkDone = view.findViewById(R.id.btn_mark_done);
        btnNext = view.findViewById(R.id.btn_next);

        // 1. 先准备一份假的 shopping list，后面你再替换成真正的数据
        initFakeShoppingList();

        // 2. 显示第一个 item
        showCurrentItem();

        // 3. 点击“Use” → 可以先简单标记一下
        btnMarkDone.setOnClickListener(v -> {
            shoppingList.get(currentIndex).done = true;
            showCurrentItem();   // 刷新一下，让你能看到 done 的效果（下面你想变颜色也行）
        });

        // 4. 点击“Next Item” → 切下一个
        btnNext.setOnClickListener(v -> {
            currentIndex++;
            if (currentIndex >= shoppingList.size()) {
                currentIndex = 0; // 简单处理：回到开头
            }
            showCurrentItem();
        });
    }

    private void initFakeShoppingList() {
        shoppingList.clear();
        shoppingList.add(new ShoppingItem("Chicken", "Go to Meat section, back right", false));
        shoppingList.add(new ShoppingItem("Brown Rice", "Aisle 5 → grains", false));
        shoppingList.add(new ShoppingItem("Egg", "Dairy corner, top left", false));
        shoppingList.add(new ShoppingItem("Corn", "Produce → middle racks", false));
        shoppingList.add(new ShoppingItem("Broccoli Crown", "Aisle 9 → Produce", false));
    }

    private void showCurrentItem() {
        ShoppingItem item = shoppingList.get(currentIndex);
        tvItemName.setText(item.name);
        tvInstruction.setText(item.instruction);

        String progress = "Item " + (currentIndex + 1) + " / " + shoppingList.size();
        if (item.done) {
            progress += " (done)";
        }
        tvProgress.setText(progress);
    }

    // 简单的数据结构，后面你要加 aisle、坐标、route id 都可以往这里塞
    private static class ShoppingItem {
        String name;
        String instruction;
        boolean done;

        ShoppingItem(String name, String instruction, boolean done) {
            this.name = name;
            this.instruction = instruction;
            this.done = done;
        }
    }
}
