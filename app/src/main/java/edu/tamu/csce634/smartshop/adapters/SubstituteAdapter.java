package edu.tamu.csce634.smartshop.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.utils.IngredientSubstitutes;

/**
 * 替代品选项适配器
 */
public class SubstituteAdapter extends RecyclerView.Adapter<SubstituteAdapter.SubstituteViewHolder> {

    public interface OnSubstituteSelectedListener {
        void onSubstituteSelected(IngredientSubstitutes.Substitute substitute);
    }

    private final List<IngredientSubstitutes.Substitute> substitutes;
    private final OnSubstituteSelectedListener listener;
    private int selectedPosition = 0; // 默认选中第一个

    public SubstituteAdapter(List<IngredientSubstitutes.Substitute> substitutes,
                             OnSubstituteSelectedListener listener) {
        this.substitutes = substitutes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubstituteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_substitute_option, parent, false);
        return new SubstituteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubstituteViewHolder holder, int position) {
        IngredientSubstitutes.Substitute substitute = substitutes.get(position);

        // ✅ 设置名称（每次都执行）
        holder.name.setText(substitute.name);

        // ✅ 设置说明（每次都执行）
        String noteText = substitute.note;
        if (substitute.quantityAdjustment != null && !substitute.quantityAdjustment.isEmpty()) {
            noteText += " • " + substitute.quantityAdjustment;
        }
        holder.note.setText(noteText);

        // ✅ 确保文字可见
        holder.name.setVisibility(View.VISIBLE);
        holder.note.setVisibility(View.VISIBLE);

        // 加载图片
        Glide.with(holder.itemView.getContext())
                .load(substitute.imageResId)
                .centerCrop()
                .placeholder(R.color.gray_200)
                .into(holder.image);

        // 选中状态显示
        boolean isSelected = position == selectedPosition;
        holder.selectedIcon.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        // 选中状态边框
        MaterialCardView card = (MaterialCardView) holder.itemView;
        if (isSelected) {
            card.setStrokeColor(holder.itemView.getContext().getColor(R.color.green_500));
            card.setStrokeWidth(6);  // ✅ 加粗边框更明显
        } else {
            card.setStrokeColor(android.graphics.Color.TRANSPARENT);
            card.setStrokeWidth(0);
        }

        // 点击事件
        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // 刷新旧位置和新位置
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);

            // 通知监听器
            if (listener != null) {
                listener.onSubstituteSelected(substitute);
            }
        });
    }

    @Override
    public int getItemCount() {
        return substitutes.size();
    }

    public IngredientSubstitutes.Substitute getSelectedSubstitute() {
        if (selectedPosition >= 0 && selectedPosition < substitutes.size()) {
            return substitutes.get(selectedPosition);
        }
        return null;
    }

    static class SubstituteViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        TextView note;
        ImageView selectedIcon;

        SubstituteViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.img_substitute);
            name = itemView.findViewById(R.id.text_substitute_name);
            note = itemView.findViewById(R.id.text_substitute_note);
            selectedIcon = itemView.findViewById(R.id.img_selected);
        }
    }
}