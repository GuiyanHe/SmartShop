package edu.tamu.csce634.smartshop.ui.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.tamu.csce634.smartshop.R;

/**
 * 适配器：将 ShoppingItem 渲染成 “卡片项”（对应 item_shopping.xml）
 * 新版布局只有：title、sub、btnReplace（文本链接）、qtyBadge（数量气泡）
 * 不再有 price / +/- 按钮，所以不要再引用那些 id
 */
public class ShoppingItemAdapter extends RecyclerView.Adapter<ShoppingItemAdapter.VH> {

    private final List<ShoppingItem> itemList;   // 数据源：购物项列表
    private final ListViewModel listViewModel;   // 用于通知 VM 重新计算总价等

    public ShoppingItemAdapter(List<ShoppingItem> list, ListViewModel vm) {
        this.itemList = list;
        this.listViewModel = vm;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载新版卡片布局
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ShoppingItem it = itemList.get(position);

        // 标题优先显示已选 SKU 名称，退化到食材名
        h.title.setText(it.selectedSkuName != null ? it.selectedSkuName : it.name);

        // 次级信息：SKU/分区等（这里先显示 aisle，你也可以拼接 SKU）
        h.sub.setText(it.aisle != null ? ("SKU: " + it.aisle) : "");

        // 数量气泡文案
        h.qtyBadge.setText(String.format("Qty: %s", formatQty(it.quantity)));

        // 点击 “Options ›” 文本：弹出 BottomSheet（根据 ingredientId）
        h.btnReplace.setOnClickListener(v -> {
            ProductOptionsBottomSheet sheet = ProductOptionsBottomSheet.newInstance(it.ingredientId);
            sheet.show(((androidx.fragment.app.FragmentActivity) v.getContext())
                    .getSupportFragmentManager(), "ProductOptionsBottomSheet");
        });

        // 点击数量气泡：简单做“+1”；长按：“-1”（最小到 0）
        h.qtyBadge.setOnClickListener(v -> {
            it.quantity = it.quantity + 1;
            h.qtyBadge.setText(String.format("Qty: %s", formatQty(it.quantity)));
            // 通知 VM：列表变了，重新算总价
            listViewModel.updateItemList(itemList);
        });

        h.qtyBadge.setOnLongClickListener(v -> {
            if (it.quantity > 0) {
                it.quantity = it.quantity - 1;
                h.qtyBadge.setText(String.format("Qty: %s", formatQty(it.quantity)));
                listViewModel.updateItemList(itemList);
            }
            return true; // 消费长按
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // 小工具：把数量显示成整数或一位小数（根据你们需要可调整）
    private String formatQty(double q) {
        if (Math.abs(q - Math.round(q)) < 1e-6) return String.valueOf((int) Math.round(q));
        return String.format("%.1f", q);
    }

    /** ViewHolder：缓存新版布局的控件引用 */
    static class VH extends RecyclerView.ViewHolder {
        TextView title;       // R.id.title
        TextView sub;         // R.id.sub
        TextView btnReplace;  // R.id.btnReplace（文本样式的“Options ›”）
        TextView qtyBadge;    // R.id.qtyBadge（绿色圆角气泡）

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            sub = itemView.findViewById(R.id.sub);
            btnReplace = itemView.findViewById(R.id.btnReplace);
            qtyBadge = itemView.findViewById(R.id.qtyBadge);
        }
    }
}
