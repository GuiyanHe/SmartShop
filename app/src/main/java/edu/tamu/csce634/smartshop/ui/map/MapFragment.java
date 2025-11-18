package edu.tamu.csce634.smartshop.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.models.ShoppingItem;
import edu.tamu.csce634.smartshop.ui.list.ListViewModel;
public class MapFragment extends Fragment {

    private ImageView imgMap;
    private TextView tvItemName;
    private TextView tvInstruction;
    private TextView tvProgress;

    // 新加的两个按钮
    private Button btnCurrentItem;
    private Button btnNext;
    private ListViewModel listViewModel;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageButton btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> {
            // 调用 NavController 的 popBackStack() 方法返回上一页
            NavHostFragment.findNavController(this).popBackStack();
        });
        // --- 1. 视图绑定 (这部分保持不变) ---
        imgMap = view.findViewById(R.id.img_map);
        tvItemName = view.findViewById(R.id.tv_item_name);
        tvInstruction = view.findViewById(R.id.tv_instruction);
        tvProgress = view.findViewById(R.id.tv_progress);
        btnCurrentItem = view.findViewById(R.id.btn_current_item);
        btnNext = view.findViewById(R.id.btn_next);

        // --- 2. ViewModel 初始化和数据观察 (核心修改) ---
        // 获取与Activity绑定的共享ViewModel实例
        listViewModel = new ViewModelProvider(requireActivity()).get(ListViewModel.class);

        // 观察ViewModel中的购物列表数据
        listViewModel.getItemList().observe(getViewLifecycleOwner(), items -> {
            // 当ViewModel中的数据更新时，这个代码块会自动执行
            shoppingList.clear(); // 清空旧数据
            if (items != null) {
                shoppingList.addAll(items); // 添加新数据
            }

            // 刷新UI
            if (!shoppingList.isEmpty()) {
                // 如果有数据，重置索引并显示第一项
                currentIndex = 0;
                showCurrentItem();
            } else {
                // 如果列表为空，显示提示信息
                tvItemName.setText("Shopping list is empty");
                tvInstruction.setText("Add items from the list page to get started.");
                tvProgress.setText("Item 0 / 0");
            }

            // 在 observe 代码块内部
            RecyclerView rv = view.findViewById(R.id.rv_shopping_list); // 先找到RecyclerView
            if (rv.getAdapter() != null) {
                rv.getAdapter().notifyDataSetChanged();
            }
        });

        // --- 3. 底部列表和 Bottom Sheet (这部分保持不变) ---
        RecyclerView rv = view.findViewById(R.id.rv_shopping_list);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(new ShoppingAdapter(shoppingList)); // Adapter现在使用会被ViewModel填充的shoppingList

        View bottomSheet = view.findViewById(R.id.bottom_sheet);
        BottomSheetBehavior.from(bottomSheet);

        // --- 4. 按钮逻辑 (这部分保持不变) ---
        if (btnCurrentItem != null) {
            btnCurrentItem.setOnClickListener(v -> {
                if (shoppingList.isEmpty()) return; // 防止空列表崩溃
                ShoppingItem cur = shoppingList.get(currentIndex);
                cur.setDone(!cur.isDone()); // 使用 getter/setter
                showCurrentItem();
            });
        }

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                if (shoppingList.isEmpty()) return; // 防止空列表崩溃
                currentIndex++;
                if (currentIndex >= shoppingList.size()) {
                    currentIndex = 0;
                }
                showCurrentItem();
            });
        }
    }

    private void showCurrentItem() {
        if (shoppingList.isEmpty() || currentIndex >= shoppingList.size()) {
            return; // 增加安全检查，防止列表为空或索引越界
        }

        ShoppingItem item = shoppingList.get(currentIndex);
        tvItemName.setText(item.getName()); // 使用 getName()
        tvInstruction.setText(item.getAisle()); // 使用 getAisle()

        String progress = "Item " + (currentIndex + 1) + " / " + shoppingList.size();
        if (item.isDone()) { // 使用 isDone()
            progress += " (done)";
        }
        tvProgress.setText(progress);

        if (btnCurrentItem != null) {
            String btnText = item.getName() + " (" + (currentIndex + 1) + "/" + shoppingList.size() + ")";
            btnCurrentItem.setText(btnText);
        }
    }


    // -------- 数据结构 --------

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
            holder.name.setText(item.getName());

            String imageUrl = item.getImageUrl();
            if (imageUrl != null && imageUrl.startsWith("res:")) {
                // 如果是 "res:" 格式，说明它是一个资源ID
                try {
                    // 从字符串中提取出数字ID
                    int resId = Integer.parseInt(imageUrl.substring(4));
                    Glide.with(holder.itemView.getContext())
                            .load(resId) // 直接加载资源ID
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_dialog_alert)
                            .into(holder.image);
                } catch (NumberFormatException e) {
                    // 如果 "res:" 后面的不是数字，显示错误图标
                    holder.image.setImageResource(android.R.drawable.ic_dialog_alert);
                }
            } else {
                // 如果是普通的 URL (http, https, etc.)，或者为空，正常处理
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl) // 正常加载URL
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_dialog_alert)
                        .into(holder.image);
            }


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
            ImageView image;
            VH(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.tv_name);
                btnUse = itemView.findViewById(R.id.btn_use);
                image = itemView.findViewById(R.id.iv_item_image);
            }
        }
    }
}
