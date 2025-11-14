# 📍 List → Map Module Interface - Quick Guide

**Version:** 1.0 | **Last Updated:** 2024-01-13

---

## 🚀 Quick Start (5 minutes)

### Step 1: Get ViewModel in `MapFragment.java`

```java
ListViewModel listViewModel = new ViewModelProvider(requireActivity())
        .get(ListViewModel.class);
```

### Step 2: Subscribe to shopping data

```java
listViewModel.getItemsByAisle().observe(getViewLifecycleOwner(), itemsByAisle -> {
    // itemsByAisle is Map<String, List<ShoppingItem>>
    // Key = "Produce Aisle 4", Value = [tofu, cabbage, ...]
    displayOnMap(itemsByAisle);
});
```

### Step 3: Mark items as picked

```java
// When user taps checkbox
listViewModel.markItemAsPicked("ing_tofu", true);
```

### Step 4: Clear data when done

```java
// ⚠️ REQUIRED: Call this before exiting Map
listViewModel.clearAllData();
```

---

## 📊 Interface Summary Table

| Interface | When to Call | Returns | Purpose |
|-----------|-------------|---------|---------|
| `getItemsByAisle()` | `onViewCreated()` | `LiveData<Map<Aisle, Items>>` | Get grouped items for map display |
| `getSummary()` | `onViewCreated()` | `ShoppingListSummary` | Show header info (total price/items/aisles) |
| `markItemAsPicked()` | User clicks checkbox | void | Mark item as collected |
| `getProgress()` | `onViewCreated()` | `LiveData<ShoppingProgress>` | Track completion % |
| `clearAllData()` | Exit/Finish shopping | void | ⚠️ **MUST CALL** - Clear cart |

---

## 📦 Data Format

### `getItemsByAisle()` Output

```java
Map<String, List<ShoppingItem>> = {
    "Produce Aisle 4": [
        ShoppingItem {
            ingredientId: "ing_tofu",     // ⭐ Use this for markItemAsPicked()
            name: "Tofu",
            quantity: 2,                   // How many to pick
            unitPrice: 2.99,
            aisle: "Produce Aisle 4",     // ⭐ Map location
            imageUrl: "res:2131231234",
            isPicked: false                // ⭐ You set this
        },
        ShoppingItem { ... }
    ],
    "Grains Aisle": [ ... ]
}
```

### `getSummary()` Output

```java
ShoppingListSummary {
    uniqueItems: 4,      // 4 different products
    totalPrice: 19.43,   // $19.43 total
    aisleCount: 3        // Visit 3 aisles
}
```

### `getProgress()` Output

```java
ShoppingProgress {
    pickedCount: 2,      // Picked 2 items
    totalCount: 4,       // Need to pick 4 total
    percentage: 50       // 50% complete
}
```

---

## 🔄 Complete Integration Code

### Copy-paste this into `MapFragment.java`:

```java
package edu.tamu.csce634.smartshop.ui.map;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import java.util.List;
import java.util.Map;
import edu.tamu.csce634.smartshop.models.ShoppingItem;
import edu.tamu.csce634.smartshop.ui.list.ListViewModel;

public class MapFragment extends Fragment {
    
    private ListViewModel listViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ========== 1. Get ViewModel ==========
        listViewModel = new ViewModelProvider(requireActivity())
                .get(ListViewModel.class);

        // ========== 2. Get shopping data ==========
        listViewModel.getItemsByAisle().observe(getViewLifecycleOwner(), itemsByAisle -> {
            if (itemsByAisle == null || itemsByAisle.isEmpty()) {
                showEmptyState();
                return;
            }
            
            // Display aisles on map
            for (Map.Entry<String, List<ShoppingItem>> entry : itemsByAisle.entrySet()) {
                String aisleName = entry.getKey();  // "Produce Aisle 4"
                List<ShoppingItem> items = entry.getValue();
                
                // TODO: Draw marker on map for this aisle
                drawAisleMarker(aisleName, items);
            }
        });

        // ========== 3. Show summary ==========
        ListViewModel.ShoppingListSummary summary = listViewModel.getSummary();
        textHeader.setText(String.format("%d items • $%.2f", 
                summary.uniqueItems, summary.totalPrice));

        // ========== 4. Track progress ==========
        listViewModel.getProgress().observe(getViewLifecycleOwner(), progress -> {
            progressBar.setProgress(progress.percentage);
            textProgress.setText(progress.pickedCount + "/" + progress.totalCount);
        });

        // ========== 5. Finish button ==========
        btnFinish.setOnClickListener(v -> {
            listViewModel.clearAllData();  // ⚠️ REQUIRED
            Toast.makeText(requireContext(), "Shopping completed!", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        });
    }

    // ========== Example: Mark item as picked ==========
    private void onItemClicked(ShoppingItem item) {
        item.isPicked = !item.isPicked;  // Toggle state
        listViewModel.markItemAsPicked(item.ingredientId, item.isPicked);
        
        // UI auto-updates via progress observer
    }
}
```

---

## ⚠️ Critical Rules

| Rule | Why | What Happens If Violated |
|------|-----|--------------------------|
| **Must call `clearAllData()` on exit** | List module expects clean state | Data duplication, cart not cleared |
| **Cannot modify `quantity`** | Only List module manages quantities | Data inconsistency |
| **Use `ingredientId` for `markItemAsPicked()`** | Unique identifier | Wrong item marked |
| **No back navigation to List** | Cart already cleared | Empty list, user confusion |

---

## 🎯 Key ShoppingItem Fields

```java
public class ShoppingItem {
    // ========== You NEED these ==========
    public String ingredientId;   // ⭐ For markItemAsPicked(id, true)
    public String name;           // Display "Tofu"
    public double quantity;       // Display "Pick 2 items"
    public String aisle;          // ⭐ Map location "Produce Aisle 4"
    public boolean isPicked;      // ⭐ You set this flag
    
    // ========== Nice to have ==========
    public String imageUrl;       // Show product image
    public double unitPrice;      // Show "$2.99 each"
    public boolean isSubstituted; // Show "🔄" badge
}
```