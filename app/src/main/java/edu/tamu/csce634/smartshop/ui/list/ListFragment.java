package edu.tamu.csce634.smartshop.ui.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import edu.tamu.csce634.smartshop.databinding.FragmentListBinding;

import java.util.ArrayList;

// Fragment 层：显示购物列表页面
public class ListFragment extends Fragment {

    private FragmentListBinding binding;
    private ListViewModel listViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listViewModel = new ViewModelProvider(requireActivity()).get(ListViewModel.class);
        TextView totalText = binding.totalText;
        RecyclerView recyclerView = binding.recycler;

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 演示数据
        ArrayList<ShoppingItem> demoList = new ArrayList<>();
        ShoppingItem item1 = new ShoppingItem();
        item1.ingredientId = "milk";
        item1.name = "Milk";
        item1.selectedSkuName = "Organic Milk";
        item1.unitPrice = 3.50;
        item1.quantity = 2;
        item1.aisle = "Dairy Aisle 3";
        demoList.add(item1);

        ShoppingItem item2 = new ShoppingItem();
        item2.ingredientId = "apple";
        item2.name = "Apple";
        item2.selectedSkuName = "Honeycrisp Apple";
        item2.unitPrice = 1.20;
        item2.quantity = 4;
        item2.aisle = "Produce Aisle 1";
        demoList.add(item2);

        // 创建并绑定适配器
        ShoppingItemAdapter adapter = new ShoppingItemAdapter(demoList, listViewModel);
        recyclerView.setAdapter(adapter);

        // 观察总价
        listViewModel.getTotal().observe(getViewLifecycleOwner(), total -> {
            totalText.setText(String.format("Total: $%.2f", total));
        });

        // 初始总价
        double sum = 0.0;
        for (ShoppingItem si : demoList) sum += si.unitPrice * si.quantity;
        listViewModel.updateItemList(demoList);
        listViewModel.setTotal(sum);

        binding.btnProceed.setOnClickListener(v ->
                totalText.setText("Proceed clicked! (demo)")
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
