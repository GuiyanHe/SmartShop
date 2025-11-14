package edu.tamu.csce634.smartshop.ui.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.adapters.SubstituteAdapter;
import edu.tamu.csce634.smartshop.models.ShoppingItem;
import edu.tamu.csce634.smartshop.utils.ConflictDetector;
import edu.tamu.csce634.smartshop.utils.IngredientSubstitutes;

/**
 * 替代品选择 BottomSheet
 * Phase 3 实现
 */
public class SubstituteSelectionBottomSheet extends BottomSheetDialogFragment {

    // 传递参数的Key
    private static final String ARG_ITEM_NAME = "item_name";
    private static final String ARG_CONFLICT_REASON = "conflict_reason";

    // UI组件
    private TextView textTitle;
    private TextView textConflictReason;
    private RecyclerView recyclerSubstitutes;
    private MaterialButton btnCancel;
    private MaterialButton btnConfirm;
    private TextView textNoSubstitutes;

    // 数据
    private ShoppingItem originalItem;
    private ConflictDetector.Conflict conflict;
    private SubstituteAdapter adapter;

    // 回调接口
    public interface OnSubstituteConfirmedListener {
        void onSubstituteConfirmed(ShoppingItem originalItem, IngredientSubstitutes.Substitute selectedSubstitute);
    }

    private OnSubstituteConfirmedListener listener;

    /**
     * 创建实例（静态工厂方法）
     */
    public static SubstituteSelectionBottomSheet newInstance(
            ShoppingItem item,
            ConflictDetector.Conflict conflict) {
        SubstituteSelectionBottomSheet sheet = new SubstituteSelectionBottomSheet();
        sheet.originalItem = item;
        sheet.conflict = conflict;

        // 传递基本信息到Bundle（用于屏幕旋转恢复）
        Bundle args = new Bundle();
        args.putString(ARG_ITEM_NAME, item.name);
        args.putString(ARG_CONFLICT_REASON, conflict.reason);
        sheet.setArguments(args);

        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_substitute_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. 绑定视图
        textTitle = view.findViewById(R.id.text_title);
        textConflictReason = view.findViewById(R.id.text_conflict_reason);
        recyclerSubstitutes = view.findViewById(R.id.recycler_substitutes);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnConfirm = view.findViewById(R.id.btn_confirm);
        textNoSubstitutes = view.findViewById(R.id.text_no_substitutes);

        // 2. 设置标题和冲突原因
        textTitle.setText("Replace: " + originalItem.name);
        textConflictReason.setText(conflict.reason);

        // 3. 检查是否有替代品
        List<IngredientSubstitutes.Substitute> substitutes = conflict.substitutes;

        if (substitutes == null || substitutes.isEmpty()) {
            // 无替代品：隐藏列表和确认按钮
            recyclerSubstitutes.setVisibility(View.GONE);
            btnConfirm.setVisibility(View.GONE);
            textNoSubstitutes.setVisibility(View.VISIBLE);

            // 取消按钮变为"关闭"
            btnCancel.setText("Close");
            btnCancel.setOnClickListener(v -> dismiss());

        } else {
            // 有替代品：显示列表
            recyclerSubstitutes.setVisibility(View.VISIBLE);
            btnConfirm.setVisibility(View.VISIBLE);
            textNoSubstitutes.setVisibility(View.GONE);

            // 4. 设置RecyclerView
            recyclerSubstitutes.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new SubstituteAdapter(substitutes, substitute -> {
                // 选择时的回调（可选：显示Toast提示）
                // Toast.makeText(requireContext(), "Selected: " + substitute.name, Toast.LENGTH_SHORT).show();
            });
            recyclerSubstitutes.setAdapter(adapter);

            // 5. 取消按钮
            btnCancel.setOnClickListener(v -> dismiss());

            // 6. 确认按钮
            btnConfirm.setOnClickListener(v -> {
                IngredientSubstitutes.Substitute selected = adapter.getSelectedSubstitute();

                if (selected == null) {
                    Toast.makeText(requireContext(),
                            "Please select a substitute",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // 显示确认Toast
                Toast.makeText(requireContext(),
                        "✓ Replaced " + originalItem.name + " with " + selected.name,
                        Toast.LENGTH_SHORT).show();

                // ✅ 标记冲突已解决（只在确认时）
                conflict.resolved = true;

                // 回调监听器
                if (listener != null) {
                    listener.onSubstituteConfirmed(originalItem, selected);
                }

                // 关闭BottomSheet
                dismiss();
            });
        }
    }

    /**
     * 设置确认监听器
     */
    public void setOnSubstituteConfirmedListener(OnSubstituteConfirmedListener listener) {
        this.listener = listener;
    }
}