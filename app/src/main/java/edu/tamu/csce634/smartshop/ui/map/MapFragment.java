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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.csce634.smartshop.R;

public class MapFragment extends Fragment {

    private ImageView imgMap;
    private TextView tvItemName;
    private TextView tvInstruction;
    private TextView tvProgress;

    // 新加的两个按钮
    private Button btnCurrentItem;
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

        // 这两个是你刚才要加回来的按钮
        btnCurrentItem = view.findViewById(R.id.btn_current_item);
        btnNext = view.findViewById(R.id.btn_next);

        // 1. 假数据
        initFakeShoppingList();

        // 2. 显示第一个
        showCurrentItem();

        // 3. 底部列表
        RecyclerView rv = view.findViewById(R.id.rv_shopping_list);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new ShoppingAdapter(shoppingList));

        // 4. bottom sheet 行为
        View bottomSheet = view.findViewById(R.id.bottom_sheet);
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
        // behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        // 5. 按钮逻辑
        // 左边按钮：先当成“标记完成/取消完成”
        if (btnCurrentItem != null) {
            btnCurrentItem.setOnClickListener(v -> {
                ShoppingItem cur = shoppingList.get(currentIndex);
                cur.done = !cur.done;
                showCurrentItem();
            });
        }

        // 右边按钮：Next Item
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                currentIndex++;
                if (currentIndex >= shoppingList.size()) {
                    currentIndex = 0;
                }
                showCurrentItem();
            });
        }
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

        // 同步到左边的按钮上：Broccoli Crown (2/5)
        if (btnCurrentItem != null) {
            String btnText = item.name + " (" + (currentIndex + 1) + "/" + shoppingList.size() + ")";
            btnCurrentItem.setText(btnText);
        }
    }

    // -------- 数据结构 --------
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

    // -------- RecyclerView 适配器 --------
    private class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.VH> {

        private final List<ShoppingItem> items;

        ShoppingAdapter(List<ShoppingItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_shopping_map, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            ShoppingItem item = items.get(position);
            holder.name.setText(item.name);

            holder.btnUse.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    currentIndex = pos;
                    showCurrentItem();
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView name;
            Button btnUse;
            VH(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.tv_name);
                btnUse = itemView.findViewById(R.id.btn_use);
            }
        }
    }
}
