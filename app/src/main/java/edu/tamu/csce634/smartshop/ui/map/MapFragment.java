package edu.tamu.csce634.smartshop.ui.map;

import android.graphics.Color;
import android.graphics.PointF;
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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.models.ShoppingItem;
import edu.tamu.csce634.smartshop.models.world.SupermarketLayout;
import edu.tamu.csce634.smartshop.ui.list.ListViewModel;

public class MapFragment extends Fragment {

    // --- UI 控件和数据变量 ---
    private ImageView imgMap; // We will keep this to get view dimensions, but make it transparent
    private TextView tvItemName, tvInstruction, tvProgress;
    private Button btnCurrentItem, btnNext;
    private PathNavigationView pathNavigationView; // 我们的“画板”
    private RecyclerView recyclerView;
    private ListViewModel listViewModel;
    private final List<ShoppingItem> shoppingList = new ArrayList<>();
    private int currentIndex = 0;
    private PointF lastLocation; // 记录上一个点的位置

    private SupermarketLayout supermarketLayout; // New: Holds the layout data

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Load data first
        loadSupermarketLayout();

        // 2. Setup all views
        setupViews(view);

        // 3. Setup data observation
        setupViewModel();

        // 4. Setup interaction
        setupClickListeners(view);
        setupBottomSheet(view);
    }

    private void loadSupermarketLayout() {
        try (InputStream is = requireContext().getResources().openRawResource(R.raw.supermarket_layout)) {
            this.supermarketLayout = new Gson().fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), SupermarketLayout.class);
        } catch (Exception e) {
            e.printStackTrace();
            this.supermarketLayout = null;
        }
    }

    private void setupViews(View view) {
        imgMap = view.findViewById(R.id.img_map);
        // Make the old background image transparent, we will draw our own
        imgMap.setImageDrawable(null);
        imgMap.setBackgroundColor(Color.WHITE); // Set a plain background color

        pathNavigationView = view.findViewById(R.id.path_navigation_view);
        // Pass the loaded layout data to our "canvas" view
        pathNavigationView.setSupermarketLayout(this.supermarketLayout);

        tvItemName = view.findViewById(R.id.tv_item_name);
        tvInstruction = view.findViewById(R.id.tv_instruction);
        tvProgress = view.findViewById(R.id.tv_progress);
        btnCurrentItem = view.findViewById(R.id.btn_current_item);
        btnNext = view.findViewById(R.id.btn_next);

        recyclerView = view.findViewById(R.id.rv_shopping_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new ShoppingAdapter(shoppingList));
    }

    private void setupViewModel() {
        listViewModel = new ViewModelProvider(requireActivity()).get(ListViewModel.class);
        listViewModel.getItemList().observe(getViewLifecycleOwner(), items -> {
            shoppingList.clear();
            if (items != null) {
                shoppingList.addAll(items);
            }

            if (!shoppingList.isEmpty()) {
                currentIndex = 0;
                // Navigation starts from the entrance
                lastLocation = new PointF(0.5f, 0.95f);
                showCurrentItem();
            } else {
                handleEmptyList();
            }

            if (recyclerView.getAdapter() != null) {
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        });
    }

    private void setupClickListeners(View view) {
        ImageButton btnBack = view.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        if (btnCurrentItem != null) {
            btnCurrentItem.setOnClickListener(v -> {
                if (shoppingList.isEmpty()) return;
                ShoppingItem cur = shoppingList.get(currentIndex);
                cur.setDone(!cur.isDone());
                showCurrentItem();
            });
        }

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                if (shoppingList.isEmpty()) return;
                ShoppingItem currentItem = shoppingList.get(currentIndex);
                if (currentItem.coordinateX != -1.0) {
                    lastLocation = new PointF((float) currentItem.coordinateX, (float) currentItem.coordinateY);
                }
                currentIndex++;
                if (currentIndex >= shoppingList.size()) {
                    currentIndex = 0;
                    lastLocation = new PointF(0.5f, 0.95f); // Loop back to entrance
                }
                showCurrentItem();
            });
        }
    }

    private void setupBottomSheet(View view) {
        View bottomSheet = view.findViewById(R.id.bottom_sheet);
        BottomSheetBehavior.from(bottomSheet);
    }

    private void handleEmptyList() {
        tvItemName.setText("No items");
        tvInstruction.setText("Your shopping list is empty.");
        tvProgress.setText("Item 0 / 0");
        if (pathNavigationView != null) {
            pathNavigationView.clearPath();
        }
    }

    private void showCurrentItem() {
        if (shoppingList.isEmpty() || currentIndex >= shoppingList.size()) {
            if (pathNavigationView != null) pathNavigationView.clearPath();
            return;
        }

        ShoppingItem item = shoppingList.get(currentIndex);
        tvItemName.setText(item.getName());
        tvInstruction.setText(item.getAisle());
        String progress = "Item " + (currentIndex + 1) + " / " + shoppingList.size();
        if (item.isDone()) {
            progress += " (done)";
        }
        tvProgress.setText(progress);
        if (btnCurrentItem != null) {
            btnCurrentItem.setText(item.getName() + " (" + (currentIndex + 1) + "/" + shoppingList.size() + ")");
        }

        if (item.coordinateX == -1.0) {
            if (pathNavigationView != null) pathNavigationView.clearPath();
            return;
        }

        PointF targetCoords = new PointF((float) item.coordinateX, (float) item.coordinateY);

        imgMap.post(() -> {
            if (lastLocation != null) {
                int mapWidth = imgMap.getWidth();
                int mapHeight = imgMap.getHeight();
                PointF startPixel = new PointF(lastLocation.x * mapWidth, lastLocation.y * mapHeight);
                PointF endPixel = new PointF(targetCoords.x * mapWidth, targetCoords.y * mapHeight);
                if (pathNavigationView != null) {
                    pathNavigationView.drawPath(startPixel, endPixel);
                }
            }
        });
    }

    // --- RecyclerView适配器部分 ---
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

            // 图片加载逻辑
            String imageUrl = item.getImageUrl();
            if (imageUrl != null && imageUrl.startsWith("res:")) {
                try {
                    int resId = Integer.parseInt(imageUrl.substring(4));
                    Glide.with(holder.itemView.getContext()).load(resId).placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.ic_dialog_alert).into(holder.image);
                } catch (NumberFormatException e) {
                    holder.image.setImageResource(android.R.drawable.ic_dialog_alert);
                }
            } else {
                Glide.with(holder.itemView.getContext()).load(imageUrl).placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.ic_dialog_alert).into(holder.image);
            }

            // 点击列表中的一项，直接导航到那里
            holder.itemView.setOnClickListener(v -> {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION || currentPosition == currentIndex) return;

                ShoppingItem currentItem = shoppingList.get(currentIndex);
                if (currentItem.coordinateX != -1.0) {
                    lastLocation = new PointF((float) currentItem.coordinateX, (float) currentItem.coordinateY);
                }

                currentIndex = currentPosition;
                showCurrentItem();
            });

            // 保留您之前的btnUse逻辑
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
