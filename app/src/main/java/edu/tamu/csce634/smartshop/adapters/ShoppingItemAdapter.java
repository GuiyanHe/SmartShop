package edu.tamu.csce634.smartshop.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class ShoppingItemAdapter extends RecyclerView.Adapter<ShoppingItemAdapter.VH> {

    private List<ShoppingItem> itemList;
    private final ListViewModel listViewModel;
    private Map<String, ConflictDetector.Conflict> conflictMap = new HashMap<>();

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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ShoppingItem it = itemList.get(position);

        String title = it.selectedSkuName != null && !it.selectedSkuName.isEmpty() ? it.selectedSkuName : it.name;
        h.title.setText(title);

        String spec = (it.skuSpec != null && !it.skuSpec.isEmpty()) ? " " + it.skuSpec : "";
        h.sub.setText("SKU: " + title + spec);

        if (it.recipeNeededStr != null && !it.recipeNeededStr.isEmpty()) {
            h.neededLabel.setVisibility(View.VISIBLE);
            h.neededLabel.setText("Recipe needs: " + it.recipeNeededStr);
        } else {
            h.neededLabel.setVisibility(View.GONE);
        }

        h.qtyBadge.setText(formatQty(it.quantity));

        if (it.imageUrl != null && !it.imageUrl.isEmpty()) {
            if (it.imageUrl.startsWith("res:")) {
                try {
                    int resId = Integer.parseInt(it.imageUrl.substring(4));
                    Glide.with(h.itemView.getContext()).load(resId).centerCrop()
                            .placeholder(android.R.color.darker_gray).into(h.image);
                } catch (NumberFormatException e) {
                    h.image.setImageResource(android.R.color.darker_gray);
                }
            } else {
                Glide.with(h.itemView.getContext()).load(it.imageUrl).centerCrop()
                        .placeholder(android.R.color.darker_gray).into(h.image);
            }
        } else {
            h.image.setImageResource(android.R.color.darker_gray);
        }

        h.btnReplace.setOnClickListener(v -> {
            ProductOptionsBottomSheet sheet = ProductOptionsBottomSheet.newInstance(it.ingredientId);
            sheet.show(((androidx.fragment.app.FragmentActivity) v.getContext())
                    .getSupportFragmentManager(), "ProductOptionsBottomSheet");
        });

        h.btnMinus.setOnClickListener(v -> {
            if (it.quantity > 0) {
                it.quantity -= 1;
                h.qtyBadge.setText(formatQty(it.quantity));
                listViewModel.recalculateTotalOnly();
            }
        });

        h.btnPlus.setOnClickListener(v -> {
            it.quantity += 1;
            h.qtyBadge.setText(formatQty(it.quantity));
            listViewModel.recalculateTotalOnly();
        });

        if (it.isSubstituted) {
            h.conflictBadge.setVisibility(View.VISIBLE);
            h.conflictBadge.setImageResource(R.drawable.ic_check_circle_gray);
            h.conflictBadge.setBackground(null);
            h.conflictBadge.setPadding(0, 0, 0, 0);
            h.conflictBadge.setColorFilter(null);
            h.conflictBadge.setBackgroundTintList(null);
            h.conflictBadge.setOnClickListener(v -> {
                String message = it.substituteDisplayName != null ? "✓ " + it.substituteDisplayName : "✓ Replaced item";
                Toast.makeText(v.getContext(), message, Toast.LENGTH_SHORT).show();
            });
            h.itemView.setOnClickListener(null);
            h.layoutConflictDetails.setVisibility(View.GONE);
            return;
        }

        String conflictKey = it.originalIngredientId != null ? it.originalIngredientId : it.ingredientId;
        ConflictDetector.Conflict conflict = conflictMap.get(conflictKey);

        if (conflict != null) {
            h.conflictBadge.setVisibility(View.VISIBLE);

            if (conflict.resolved) {
                h.conflictBadge.setImageResource(R.drawable.ic_check_circle_gray);
                h.conflictBadge.setBackground(null);
                h.conflictBadge.setPadding(0, 0, 0, 0);
                h.conflictBadge.setColorFilter(null);
                h.conflictBadge.setBackgroundTintList(null);
                h.conflictBadge.setOnClickListener(v ->
                        Toast.makeText(v.getContext(), "✓ Conflict resolved (Set to 0)", Toast.LENGTH_SHORT).show());
                h.itemView.setOnClickListener(null);
                h.layoutConflictDetails.setVisibility(View.GONE);
            } else {
                h.conflictBadge.setImageResource(R.drawable.ic_warning);
                h.conflictBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        h.itemView.getContext().getColor(R.color.red_500)));
                h.conflictBadge.setColorFilter(0xFFFFFFFF);
                h.textConflictMessage.setText(conflict.reason);

                View.OnClickListener toggleConflictDetails = v -> {
                    if (h.layoutConflictDetails.getVisibility() == View.VISIBLE) {
                        h.layoutConflictDetails.setVisibility(View.GONE);
                    } else {
                        h.layoutConflictDetails.setVisibility(View.VISIBLE);
                    }
                };

                h.itemView.setOnClickListener(toggleConflictDetails);
                h.conflictBadge.setOnClickListener(toggleConflictDetails);

                h.btnSetZero.setOnClickListener(v -> {
                    it.quantity = 0;
                    h.qtyBadge.setText(formatQty(it.quantity));
                    conflict.resolved = true;

                    h.conflictBadge.setImageResource(R.drawable.ic_check_circle_gray);
                    h.conflictBadge.setBackground(null);
                    h.conflictBadge.setPadding(0, 0, 0, 0);
                    h.conflictBadge.setColorFilter(null);
                    h.conflictBadge.setBackgroundTintList(null);
                    h.layoutConflictDetails.setVisibility(View.GONE);

                    Toast.makeText(v.getContext(), "✓ " + it.name + " set to 0", Toast.LENGTH_SHORT).show();
                    listViewModel.recalculateTotalOnly();
                });

                h.btnReplaceConflict.setOnClickListener(v -> {
                    if (substituteListener != null) {
                        substituteListener.onSubstituteRequest(it, conflict);
                    }
                });

                if (conflict.substitutes == null || conflict.substitutes.isEmpty()) {
                    h.btnReplaceConflict.setEnabled(false);
                    h.btnReplaceConflict.setText("No substitutes");
                    h.btnReplaceConflict.setAlpha(0.5f);
                } else {
                    h.btnReplaceConflict.setEnabled(true);
                    h.btnReplaceConflict.setText("Replace");
                    h.btnReplaceConflict.setAlpha(1.0f);
                }
            }
        } else {
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
        if (q <= 0) return "0";
        if (Math.abs(q - Math.round(q)) < 1e-6) return String.valueOf((int) Math.round(q));
        return String.format(java.util.Locale.US, "%.1f", q);
    }

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

    public void setOnSubstituteRequestListener(OnSubstituteRequestListener listener) {
        this.substituteListener = listener;
    }

    public List<ConflictDetector.Conflict> getConflicts() {
        return new java.util.ArrayList<>(conflictMap.values());
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView image, conflictBadge;
        TextView title, sub, btnReplace, qtyBadge, neededLabel, textConflictMessage;
        ImageButton btnMinus, btnPlus;
        View layoutConflictDetails;
        com.google.android.material.button.MaterialButton btnSetZero, btnReplaceConflict;

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
            conflictBadge = itemView.findViewById(R.id.img_conflict_badge);
            layoutConflictDetails = itemView.findViewById(R.id.layout_conflict_details);
            textConflictMessage = itemView.findViewById(R.id.text_conflict_message);
            btnSetZero = itemView.findViewById(R.id.btn_set_zero);
            btnReplaceConflict = itemView.findViewById(R.id.btn_replace_conflict);
        }
    }
}