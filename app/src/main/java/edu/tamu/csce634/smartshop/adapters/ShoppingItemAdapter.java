package edu.tamu.csce634.smartshop.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.models.ShoppingItem;
import edu.tamu.csce634.smartshop.ui.list.ListViewModel;
import edu.tamu.csce634.smartshop.ui.list.ProductOptionsBottomSheet;
import edu.tamu.csce634.smartshop.utils.ConflictDetector;

/**
 * 适配器：将 ShoppingItem 渲染成 “卡片项”（对应 item_shopping.xml）
 * 右侧有 - / Qty / + 控件；SKU 行显示 selectedSkuName + skuSpec；
 * 左侧 ImageView 用 Glide 加载图片
 */
public class ShoppingItemAdapter extends RecyclerView.Adapter<ShoppingItemAdapter.VH> {

    private List<ShoppingItem> itemList;   // 数据源
    private final ListViewModel listViewModel;   // 通知 VM 更新总价等

    // 冲突信息映射（ingredientId -> Conflict）
    private Map<String, ConflictDetector.Conflict> conflictMap = new HashMap<>();

    // 替代品选择监听器
    public interface OnSubstituteRequestListener {
        void onSubstituteRequest(ShoppingItem item, ConflictDetector.Conflict conflict);
    }
    private OnSubstituteRequestListener substituteListener;
    public ShoppingItemAdapter(List<ShoppingItem> list, ListViewModel vm) {
        this.itemList = list;
        this.listViewModel = vm;
    }

    public void updateData(List<ShoppingItem> newList) {
        this.itemList = newList;
        notifyDataSetChanged(); // 全量刷新（后续可优化为DiffUtil）
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

        // 显示Recipe需求量
        if (it.recipeNeededStr != null && !it.recipeNeededStr.isEmpty()) {
            h.neededLabel.setVisibility(View.VISIBLE);
            h.neededLabel.setText("Recipe needs: " + it.recipeNeededStr);
        } else {
            h.neededLabel.setVisibility(View.GONE);
        }

        // Qty 显示
        h.qtyBadge.setText(String.format("Qty: %s", formatQty(it.quantity)));

        // 加载图片（支持：drawable资源ID / 网络链接）
        if (it.imageUrl != null && !it.imageUrl.isEmpty()) {
            if (it.imageUrl.startsWith("res:")) {
                // Recipe模块的drawable资源ID
                try {
                    int resId = Integer.parseInt(it.imageUrl.substring(4));
                    Glide.with(h.itemView.getContext())
                            .load(resId)
                            .centerCrop()
                            .placeholder(android.R.color.darker_gray)
                            .into(h.image);
                } catch (NumberFormatException e) {
                    h.image.setImageResource(android.R.color.darker_gray);
                }
            } else {
                // HTTP/HTTPS网络链接
                Glide.with(h.itemView.getContext())
                        .load(it.imageUrl)
                        .centerCrop()
                        .placeholder(android.R.color.darker_gray)
                        .into(h.image);
            }
        } else {
            // 无图片时显示默认占位图
            h.image.setImageResource(android.R.color.darker_gray);
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
                listViewModel.recalculateTotalOnly();
            }
        });

        // “+” 数量加一
        h.btnPlus.setOnClickListener(v -> {
            it.quantity += 1;
            h.qtyBadge.setText(String.format("Qty: %s", formatQty(it.quantity)));
            listViewModel.recalculateTotalOnly();
        });
        // === 新增：冲突处理 ===

        // 检查是否有冲突
        ConflictDetector.Conflict conflict = conflictMap.get(it.ingredientId);

        if (conflict != null) {
            // 显示冲突徽章
            h.conflictBadge.setVisibility(View.VISIBLE);

            // 设置冲突信息
            h.textConflictMessage.setText(conflict.reason);

            // 点击卡片展开/收起冲突详情
            h.itemView.setOnClickListener(v -> {
                if (h.layoutConflictDetails.getVisibility() == View.VISIBLE) {
                    h.layoutConflictDetails.setVisibility(View.GONE);
                } else {
                    h.layoutConflictDetails.setVisibility(View.VISIBLE);
                }
            });

            // "Set to 0" 按钮
            h.btnSetZero.setOnClickListener(v -> {
                it.quantity = 0;
                h.qtyBadge.setText("Qty: 0");
                conflict.resolved = true;
                h.conflictBadge.setImageResource(android.R.drawable.checkbox_on_background);
                h.conflictBadge.setColorFilter(v.getContext().getColor(R.color.green_500));
                h.layoutConflictDetails.setVisibility(View.GONE);
                listViewModel.recalculateTotalOnly();
            });

            // "Replace" 按钮
            h.btnReplaceConflict.setOnClickListener(v -> {
                if (substituteListener != null) {
                    substituteListener.onSubstituteRequest(it, conflict);
                }
            });

            // 如果没有替代品，禁用Replace按钮
            if (conflict.substitutes == null || conflict.substitutes.isEmpty()) {
                h.btnReplaceConflict.setEnabled(false);
                h.btnReplaceConflict.setText("No substitutes");
            } else {
                h.btnReplaceConflict.setEnabled(true);
                h.btnReplaceConflict.setText("Replace");
            }

        } else {
            // 没有冲突，隐藏相关UI
            h.conflictBadge.setVisibility(View.GONE);
            h.layoutConflictDetails.setVisibility(View.GONE);
            h.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    private String formatQty(double q) {
        if (Math.abs(q - Math.round(q)) < 1e-6) return String.valueOf((int) Math.round(q));
        return String.format("%.1f", q);
    }

    /**
     * 设置冲突信息
     */
    public void setConflicts(List<ConflictDetector.Conflict> conflicts) {
        conflictMap.clear();
        if (conflicts != null) {
            for (ConflictDetector.Conflict c : conflicts) {
                if (c.item != null && c.item.ingredientId != null) {
                    conflictMap.put(c.item.ingredientId, c);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 设置替代品请求监听器
     */
    public void setOnSubstituteRequestListener(OnSubstituteRequestListener listener) {
        this.substituteListener = listener;
    }
    static class VH extends RecyclerView.ViewHolder {
        ImageView image;      // R.id.itemImage
        TextView title;       // R.id.title
        TextView sub;         // R.id.sub
        TextView btnReplace;  // R.id.btnReplace
        TextView qtyBadge;    // R.id/qtyBadge（中间的 Qty: x）
        TextView btnMinus;    // R.id/btnMinus（左侧 -）
        TextView btnPlus;     // R.id/btnPlus（右侧 +）
        TextView neededLabel;
        // 新增：冲突相关视图
        ImageView conflictBadge;
        LinearLayout layoutConflictDetails;
        TextView textConflictMessage;
        com.google.android.material.button.MaterialButton btnSetZero;
        com.google.android.material.button.MaterialButton btnReplaceConflict;

        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.itemImage);
            title = itemView.findViewById(R.id.title);
            sub = itemView.findViewById(R.id.sub);
            btnReplace = itemView.findViewById(R.id.btnReplace);
            qtyBadge = itemView.findViewById(R.id.qtyBadge);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            neededLabel = itemView.findViewById(R.id.neededLabel);
            // 新增视图引用
            conflictBadge = itemView.findViewById(R.id.img_conflict_badge);
            layoutConflictDetails = itemView.findViewById(R.id.layout_conflict_details);
            textConflictMessage = itemView.findViewById(R.id.text_conflict_message);
            btnSetZero = itemView.findViewById(R.id.btn_set_zero);
            btnReplaceConflict = itemView.findViewById(R.id.btn_replace_conflict);
        }
    }
}
