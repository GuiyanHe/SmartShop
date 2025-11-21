package edu.tamu.csce634.smartshop.ui.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONObject;

import edu.tamu.csce634.smartshop.R;
import edu.tamu.csce634.smartshop.data.PresetRepository;

// BottomSheet 弹窗，用于选择替换的商品类型
public class ProductOptionsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_ING_ID = "ARG_ING_ID";

    // 创建实例并传入当前商品的 ingredientId
    public static ProductOptionsBottomSheet newInstance(String ingredientId) {
        ProductOptionsBottomSheet sheet = new ProductOptionsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_ING_ID, ingredientId);
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 复用原 layout 作为容器（顶层是 LinearLayout）
        View root = inflater.inflate(R.layout.sheet_product_options, container, false);
        android.widget.LinearLayout containerView = (android.widget.LinearLayout) root;

        String ingredientId = getArguments() != null ? getArguments().getString(ARG_ING_ID) : "";
        ListViewModel vm = new ViewModelProvider(requireActivity()).get(ListViewModel.class);
        PresetRepository repo = new PresetRepository(requireContext());

        try {
            java.util.List<JSONObject> options = repo.getOptionsFor(ingredientId);

            // 清空容器中原有的两个固定按钮
            containerView.removeAllViews();

            // 动态添加“品牌+规格+是否有机+价格”的选项按钮
            for (JSONObject o : options) {
                String display = o.optString("displayName");
                String size    = o.optString("size");
                boolean org    = o.optBoolean("isOrganic", false);
                double price   = o.optDouble("unitPrice", 0.0);
                String img     = o.optString("imageUrl", "");

                String label = display + " • " + size + (org ? " • Organic" : "") + " • $" + String.format("%.2f", price);

                android.widget.Button btn = new android.widget.Button(requireContext());
                btn.setAllCaps(false);
                btn.setText(label);
                btn.setTextColor(0xFFFFFFFF);
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));

                android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.topMargin = (int) (8 * getResources().getDisplayMetrics().density);
                btn.setLayoutParams(lp);

                btn.setOnClickListener(v -> {
                    // 更新 SKU 信息（名称、价格、规格、图片）
                    vm.replaceSkuFull(ingredientId, display, price, size, img);

                    // ✅ 重新计算购买数量（基于新的包装规格）
                    vm.recalculateQuantityForItem(ingredientId);

                    dismiss();
                });
                containerView.addView(btn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return root;
    }


}
