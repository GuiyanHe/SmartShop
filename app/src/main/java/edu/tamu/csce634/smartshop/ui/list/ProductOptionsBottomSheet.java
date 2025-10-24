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

import edu.tamu.csce634.smartshop.R;

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
        View root = inflater.inflate(R.layout.sheet_product_options, container, false);

        Button btnOrganic = root.findViewById(R.id.optionOrganic);
        Button btnRegular = root.findViewById(R.id.optionRegular);

        // 获取传入的 ingredientId
        String ingredientId = getArguments() != null ? getArguments().getString(ARG_ING_ID) : "";

        // 通过 Activity 获取共享的 ViewModel
        ListViewModel vm = new ViewModelProvider(requireActivity()).get(ListViewModel.class);

        // 有机商品选项
        btnOrganic.setOnClickListener(v -> {
            vm.replaceSku(ingredientId, "Organic Option", 3.99);
            dismiss();
        });

        // 普通商品选项
        btnRegular.setOnClickListener(v -> {
            vm.replaceSku(ingredientId, "Regular Option", 2.49);
            dismiss();
        });

        return root;
    }
}
