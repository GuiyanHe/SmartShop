package edu.tamu.csce634.smartshop.ui.map;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

    private ImageView imgMap;
    private TextView tvItemName, tvInstruction, tvProgress;
    private Button btnCurrentItem, btnNext;
    private PathNavigationView pathNavigationView;
    private RecyclerView recyclerView;
    private ListViewModel listViewModel;
    private final List<ShoppingItem> shoppingList = new ArrayList<>();
    private int currentIndex = 0;
    private PointF lastLocation;

    private SupermarketLayout supermarketLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadSupermarketLayout();
        setupToolbar(view);
        setupViews(view);
        setupViewModel();
        setupClickListeners();
    }

    private void loadSupermarketLayout() {
        try (InputStream is = requireContext().getResources().openRawResource(R.raw.supermarket_layout);
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            this.supermarketLayout = new Gson().fromJson(reader, SupermarketLayout.class);
        } catch (Exception e) {
            e.printStackTrace();
            this.supermarketLayout = null;
        }
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    }

    private void setupViews(View view) {
        imgMap = view.findViewById(R.id.img_map);
        pathNavigationView = view.findViewById(R.id.path_navigation_view);
        View bottomSheet = view.findViewById(R.id.bottom_sheet);
        tvItemName = bottomSheet.findViewById(R.id.tv_item_name);
        tvInstruction = bottomSheet.findViewById(R.id.tv_instruction);
        tvProgress = bottomSheet.findViewById(R.id.tv_progress);
        btnCurrentItem = bottomSheet.findViewById(R.id.btn_current_item);
        btnNext = bottomSheet.findViewById(R.id.btn_next);
        recyclerView = bottomSheet.findViewById(R.id.rv_shopping_list);

        imgMap.setImageDrawable(null);
        imgMap.setBackgroundColor(Color.WHITE);
        pathNavigationView.setSupermarketLayout(this.supermarketLayout);
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
                lastLocation = new PointF(0.5f, 0.95f); // Entrance
                showCurrentItem();
            } else {
                handleEmptyList();
            }
            if (recyclerView.getAdapter() != null) {
                recyclerView.getAdapter().notifyDataSetChanged();
            }
        });
    }

    private void setupClickListeners() {
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
                if (currentItem.coordinateX > 0 && currentItem.coordinateY > 0) {
                    lastLocation = new PointF((float) currentItem.coordinateX, (float) currentItem.coordinateY);
                }
                currentIndex++;
                if (currentIndex >= shoppingList.size()) {
                    currentIndex = 0;
                    lastLocation = new PointF(0.5f, 0.95f); // Loop back
                }
                showCurrentItem();
            });
        }
    }

    private void handleEmptyList() {
        if (tvItemName != null) tvItemName.setText("No items");
        if (tvInstruction != null) tvInstruction.setText("Your shopping list is empty.");
        if (tvProgress != null) tvProgress.setText("Item 0 / 0");
        if (pathNavigationView != null) pathNavigationView.clearPath();
    }

    private void showCurrentItem() {
        if (shoppingList.isEmpty() || currentIndex >= shoppingList.size()) {
            if (pathNavigationView != null) pathNavigationView.clearPath();
            return;
        }

        ShoppingItem item = shoppingList.get(currentIndex);
        if (tvItemName != null) tvItemName.setText(item.getName());
        if (tvInstruction != null) tvInstruction.setText(item.getAisle());
        if (tvProgress != null) {
            String progress = "Item " + (currentIndex + 1) + " / " + shoppingList.size();
            if (item.isDone()) progress += " (done)";
            tvProgress.setText(progress);
        }
        if (btnCurrentItem != null) {
            btnCurrentItem.setText(item.getName() + " (" + (currentIndex + 1) + "/" + shoppingList.size() + ")");
        }

        if (item.coordinateX <= 0 || item.coordinateY <= 0) {
            Log.e("MapFragment", "Item has invalid (zero or negative) coordinates: " + item.getName() + " (Category: " + item.getCategory() + ")");
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
                if (pathNavigationView != null) pathNavigationView.drawPath(startPixel, endPixel);
            }
        });
    }

    private class ShoppingAdapter extends RecyclerView.Adapter<ShoppingAdapter.VH> {
        private final List<ShoppingItem> items;
        ShoppingAdapter(List<ShoppingItem> items) { this.items = items; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping_map, parent, false)); }
        @Override public void onBindViewHolder(@NonNull VH holder, int position) {
            ShoppingItem item = items.get(position);
            holder.name.setText(item.getName());
            String imageUrl = item.getImageUrl();
            if (imageUrl != null && imageUrl.startsWith("res:")) {
                try {
                    int resId = Integer.parseInt(imageUrl.substring(4));
                    Glide.with(holder.itemView.getContext()).load(resId).placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.ic_dialog_alert).into(holder.image);
                } catch (NumberFormatException e) { holder.image.setImageResource(android.R.drawable.ic_dialog_alert); }
            } else { Glide.with(holder.itemView.getContext()).load(imageUrl).placeholder(android.R.drawable.ic_menu_gallery).error(android.R.drawable.ic_dialog_alert).into(holder.image); }
            holder.itemView.setOnClickListener(v -> {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION || currentPosition == currentIndex) return;
                ShoppingItem currentItem = shoppingList.get(currentIndex);
                if (currentItem.coordinateX > 0 && currentItem.coordinateY > 0) { lastLocation = new PointF((float) currentItem.coordinateX, (float) currentItem.coordinateY); }
                currentIndex = currentPosition;
                showCurrentItem();
            });
        }
        @Override public int getItemCount() { return items.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView name; ImageView image;
            VH(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.tv_name);
                image = itemView.findViewById(R.id.iv_item_image);
            }
        }
    }
}
