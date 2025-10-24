package edu.tamu.csce634.smartshop.ui.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import edu.tamu.csce634.smartshop.R;

/**
 * 适配器：将 ShoppingItem 渲染成 “卡片项”（对应 item_shopping.xml）
 * 右侧有 - / Qty / + 控件；SKU 行显示 selectedSkuName + skuSpec；
 * 左侧 ImageView 用 Glide 加载图片
 */
public class ShoppingItemAdapter extends RecyclerView.Adapter<ShoppingItemAdapter.VH> {

    private final List<ShoppingItem> itemList;   // 数据源
    private final ListViewModel listViewModel;   // 通知 VM 更新总价等

    public ShoppingItemAdapter(List<ShoppingItem> list, ListViewModel vm) {
        this.itemList = list;
        this.listViewModel = vm;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ShoppingItem it = itemList.get(position);

        // 标题：优先显示 selectedSkuName，否则回退到 name
        String title = it.selectedSkuName != null && !it.selectedSkuName.isEmpty()
                ? it.selectedSkuName : it.name;
        h.title.setText(title);

        // SKU 行：selectedSkuName + skuSpec（若有）
        String spec = (it.skuSpec != null && !it.skuSpec.isEmpty()) ? " " + it.skuSpec : "";
        h.sub.setText("SKU: " + title + spec);

        // Qty 显示
        h.qtyBadge.setText(String.format("Qty: %s", formatQty(it.quantity)));

        // 加载图片
        if (it.imageUrl != null && it.imageUrl.startsWith("asset:")) {
            String fileName = it.imageUrl.substring(6); // e.g. "milk.jpeg"
            String uri = "file:///android_asset/items/" + fileName;
            Glide.with(h.itemView.getContext())
                    .load(uri)
                    .centerCrop()
                    .placeholder(android.R.color.darker_gray)
                    .into(h.image);
        } else {
            Glide.with(h.itemView.getContext())
                    .load(it.imageUrl)
                    .centerCrop()
                    .placeholder(android.R.color.darker_gray)
                    .into(h.image);
        }

        // Options：弹出 BottomSheet
        h.btnReplace.setOnClickListener(v -> {
            ProductOptionsBottomSheet sheet = ProductOptionsBottomSheet.newInstance(it.ingredientId);
            sheet.show(((androidx.fragment.app.FragmentActivity) v.getContext())
                    .getSupportFragmentManager(), "ProductOptionsBottomSheet");
        });

        // “-” 数量减一（到 0 为止）
        h.btnMinus.setOnClickListener(v -> {
            if (it.quantity > 0) {
                it.quantity -= 1;
                h.qtyBadge.setText(String.format("Qty: %s", formatQty(it.quantity)));
                listViewModel.updateItemList(itemList); // 通知 VM 重新计算总价
            }
        });

        // “+” 数量加一
        h.btnPlus.setOnClickListener(v -> {
            it.quantity += 1;
            h.qtyBadge.setText(String.format("Qty: %s", formatQty(it.quantity)));
            listViewModel.updateItemList(itemList);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    private String formatQty(double q) {
        if (Math.abs(q - Math.round(q)) < 1e-6) return String.valueOf((int) Math.round(q));
        return String.format("%.1f", q);
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image;      // R.id.itemImage
        TextView title;       // R.id.title
        TextView sub;         // R.id.sub
        TextView btnReplace;  // R.id.btnReplace
        TextView qtyBadge;    // R.id/qtyBadge（中间的 Qty: x）
        TextView btnMinus;    // R.id/btnMinus（左侧 -）
        TextView btnPlus;     // R.id/btnPlus（右侧 +）

        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.itemImage);
            title = itemView.findViewById(R.id.title);
            sub = itemView.findViewById(R.id.sub);
            btnReplace = itemView.findViewById(R.id.btnReplace);
            qtyBadge = itemView.findViewById(R.id.qtyBadge);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }
    }
}
