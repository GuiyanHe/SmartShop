
# Map模块数据接口文档

## 获取数据

```java
// 在 MapFragment.onViewCreated() 中
ListViewModel listViewModel = new ViewModelProvider(requireActivity())
        .get(ListViewModel.class);

// 获取按通道分组的商品（实时更新）
listViewModel.getItemsByAisle().observe(getViewLifecycleOwner(), itemsByAisle -> {
    // itemsByAisle: Map<String, List<ShoppingItem>>
    // 在地图上渲染路线
});
```

## 可用接口

| 方法 | 返回类型 | 说明 |
|------|----------|------|
| `getItemsByAisle()` | `LiveData<Map<String, List<ShoppingItem>>>` | 按通道分组的商品 |
| `getSummary()` | `ShoppingListSummary` | 摘要信息（总价、件数、通道数）|
| `markItemAsPicked(id, true)` | void | 标记商品已拾取 |
| `getProgress()` | `LiveData<ShoppingProgress>` | 购物进度（X/Y） |
| `clearAllData()` | void | **完成购物后必须调用** |

## ShoppingItem 字段

```java
item.ingredientId   // 商品ID（用于markAsPicked）
item.name           // 商品名称
item.aisle          // 通道位置（如 "Produce Aisle 4"）⭐
item.quantity       // 购买数量
item.unitPrice      // 单价
item.imageUrl       // 图片（格式："res:123456"）
item.isPicked       // 是否已拾取（Map模块设置）⭐
```

## 完整示例

```java
public class MapFragment extends Fragment {
    private ListViewModel listViewModel;
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 1. 获取ViewModel
        listViewModel = new ViewModelProvider(requireActivity()).get(ListViewModel.class);
        
        // 2. 显示摘要
        ShoppingListSummary summary = listViewModel.getSummary();
        textSummary.setText(summary.uniqueItems + " items • $" + summary.totalPrice);
        
        // 3. 渲染地图
        listViewModel.getItemsByAisle().observe(getViewLifecycleOwner(), itemsByAisle -> {
            for (Map.Entry<String, List<ShoppingItem>> entry : itemsByAisle.entrySet()) {
                String aisleName = entry.getKey();
                List<ShoppingItem> items = entry.getValue();
                // 在地图上标记该通道
            }
        });
        
        // 4. 监听进度
        listViewModel.getProgress().observe(getViewLifecycleOwner(), progress -> {
            progressBar.setProgress(progress.percentage);
        });
        
        // 5. 完成购物
        btnFinish.setOnClickListener(v -> {
            listViewModel.clearAllData();
            Toast.makeText(requireContext(), "✓ Shopping completed", Toast.LENGTH_SHORT).show();
        });
    }
    
    // 用户勾选商品
    private void onItemChecked(String ingredientId) {
        listViewModel.markItemAsPicked(ingredientId, true);
    }
}
```

## 注意事项

⚠️ **进入Map后List已清空Recipe购物车，用户无法返回**

⚠️ **Map只能标记isPicked，不能修改quantity**

⚠️ **完成购物后必须调用 clearAllData()**