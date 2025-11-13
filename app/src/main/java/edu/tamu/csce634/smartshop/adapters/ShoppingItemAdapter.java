package edu.tamu.csce634.smartshop.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
 * 适配器：将 ShoppingItem 渲染成卡片项
 * 支持冲突检测、替代品选择、数量调整
 */
public class ShoppingItemAdapter extends RecyclerView.Adapter<ShoppingItemAdapter.VH> {

    private List<ShoppingItem> itemList;
    private final ListViewModel listViewModel;

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
        notifyDataSetChanged();
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

        // ========== 基础信息显示 ==========

        // 标题：优先显示 selectedSkuName
        String title = it.selectedSkuName != null && !it.selectedSkuName.isEmpty()
                ? it.selectedSkuName : it.name;
        h.title.setText(title);

        // SKU 行
        String spec = (it.skuSpec != null && !it.skuSpec.isEmpty()) ? " " + it.skuSpec : "";
        h.sub.setText("SKU: " + title + spec);

        // 显示 Recipe 需求量
        if (it.recipeNeededStr != null && !it.recipeNeededStr.isEmpty()) {
            h.neededLabel.setVisibility(View.VISIBLE);
            h.neededLabel.setText("Recipe needs: " + it.recipeNeededStr);
        } else {
            h.neededLabel.setVisibility(View.GONE);
        }

        // Qty 显示（纯数字）
        h.qtyBadge.setText(formatQty(it.quantity));

        // ========== 图片加载 ==========

        if (it.imageUrl != null && !it.imageUrl.isEmpty()) {
            if (it.imageUrl.startsWith("res:")) {
                // Drawable 资源ID
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
                // 网络链接
                Glide.with(h.itemView.getContext())
                        .load(it.imageUrl)
                        .centerCrop()
                        .placeholder(android.R.color.darker_gray)
                        .into(h.image);
            }
        } else {
            h.image.setImageResource(android.R.color.darker_gray);
        }

        // ========== Options 按钮 ==========

        h.btnReplace.setOnClickListener(v -> {
            ProductOptionsBottomSheet sheet = ProductOptionsBottomSheet.newInstance(it.ingredientId);
            sheet.show(((androidx.fragment.app.FragmentActivity) v.getContext())
                    .getSupportFragmentManager(), "ProductOptionsBottomSheet");
        });

        // ========== 数量调整按钮 ==========

        // "-" 数量减一
        h.btnMinus.setOnClickListener(v -> {
            if (it.quantity > 0) {
                it.quantity -= 1;
                h.qtyBadge.setText(formatQty(it.quantity));
                listViewModel.recalculateTotalOnly();
            }
        });

        // "+" 数量加一
        h.btnPlus.setOnClickListener(v -> {
            it.quantity += 1;
            h.qtyBadge.setText(formatQty(it.quantity));
            listViewModel.recalculateTotalOnly();
        });

// ========== 冲突处理 ==========

        ConflictDetector.Conflict conflict = conflictMap.get(it.ingredientId);

        if (conflict != null) {
            // 显示冲突徽章
            h.conflictBadge.setVisibility(View.VISIBLE);

            // ✅ 根据解决状态设置徽章样式（完全切换图标和背景）
            if (conflict.resolved) {
                // 已解决：灰色圆形打勾（完全替换，无叠加）
                h.conflictBadge.setImageResource(R.drawable.ic_check_circle_gray);
                h.conflictBadge.setBackground(null);  // ✅ 移除红色圆形背景
                h.conflictBadge.setPadding(0, 0, 0, 0);  // ✅ 移除内边距
                h.conflictBadge.setColorFilter(null);  // ✅ 移除颜色滤镜
                h.conflictBadge.setBackgroundTintList(null);  // ✅ 移除背景着色
            } else {
                // 未解决：红色圆形警告
                h.conflictBadge.setImageResource(R.drawable.ic_warning);
//                h.conflictBadge.setBackgroundResource(R.drawable.circle_background);  // ✅ 重新设置圆形背景
                h.conflictBadge.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                h.itemView.getContext().getColor(R.color.red_500)));
//                h.conflictBadge.setPadding(8, 8, 8, 8);  // ✅ 恢复内边距
                h.conflictBadge.setColorFilter(0xFFFFFFFF);  // 白色图标
            }

            // 设置冲突信息
            h.textConflictMessage.setText(conflict.reason);

            // 点击事件（徽章或卡片都可以展开）
            View.OnClickListener toggleConflictDetails = v -> {
                if (h.layoutConflictDetails.getVisibility() == View.VISIBLE) {
                    h.layoutConflictDetails.setVisibility(View.GONE);
                } else {
                    h.layoutConflictDetails.setVisibility(View.VISIBLE);
                }
            };

            h.itemView.setOnClickListener(toggleConflictDetails);
            h.conflictBadge.setOnClickListener(toggleConflictDetails);

            // "Set to 0" 按钮
            h.btnSetZero.setOnClickListener(v -> {
                it.quantity = 0;
                h.qtyBadge.setText(formatQty(it.quantity));

                // 标记为已解决
                conflict.resolved = true;

                // ✅ 徽章完全切换为灰色圆形打勾
                h.conflictBadge.setImageResource(R.drawable.ic_check_circle_gray);
                h.conflictBadge.setBackground(null);  // 移除红色背景
                h.conflictBadge.setPadding(0, 0, 0, 0);
                h.conflictBadge.setColorFilter(null);
                h.conflictBadge.setBackgroundTintList(null);

                // 收起冲突详情
                h.layoutConflictDetails.setVisibility(View.GONE);

                android.widget.Toast.makeText(v.getContext(),
                        "✓ " + it.name + " set to 0",
                        android.widget.Toast.LENGTH_SHORT).show();

                listViewModel.recalculateTotalOnly();
            });

            // "Replace" 按钮
            h.btnReplaceConflict.setOnClickListener(v -> {
                if (substituteListener != null) {
                    substituteListener.onSubstituteRequest(it, conflict);
                }
            });

            // 如果没有替代品，禁用 Replace 按钮
            if (conflict.substitutes == null || conflict.substitutes.isEmpty()) {
                h.btnReplaceConflict.setEnabled(false);
                h.btnReplaceConflict.setText("No substitutes");
                h.btnReplaceConflict.setAlpha(0.5f);
            } else {
                h.btnReplaceConflict.setEnabled(true);
                h.btnReplaceConflict.setText("Replace");
                h.btnReplaceConflict.setAlpha(1.0f);
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

    /**
     * 格式化数量显示
     */
    private String formatQty(double q) {
        if (q <= 0) return "0";
        if (Math.abs(q - Math.round(q)) < 1e-6) {
            return String.valueOf((int) Math.round(q));
        }
        return String.format(java.util.Locale.US, "%.1f", q);
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

    /**
     * 获取当前所有冲突（用于未解决冲突检测）
     */
    public List<ConflictDetector.Conflict> getConflicts() {
        return new java.util.ArrayList<>(conflictMap.values());
    }

    /**
     * ViewHolder
     */
    static class VH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView sub;
        TextView btnReplace;
        TextView qtyBadge;
        ImageButton btnMinus;
        ImageButton btnPlus;
        TextView neededLabel;

        // 冲突相关视图
        ImageView conflictBadge;
        View layoutConflictDetails;
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

            // 冲突视图
            conflictBadge = itemView.findViewById(R.id.img_conflict_badge);
            layoutConflictDetails = itemView.findViewById(R.id.layout_conflict_details);
            textConflictMessage = itemView.findViewById(R.id.text_conflict_message);
            btnSetZero = itemView.findViewById(R.id.btn_set_zero);
            btnReplaceConflict = itemView.findViewById(R.id.btn_replace_conflict);
        }
    }
}