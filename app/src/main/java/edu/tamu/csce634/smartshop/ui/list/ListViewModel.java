package edu.tamu.csce634.smartshop.ui.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.csce634.smartshop.models.ShoppingItem;
import edu.tamu.csce634.smartshop.utils.QuantityParser;

public class ListViewModel extends ViewModel {
    private final MutableLiveData<Double> totalLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ShoppingItem>> itemListLiveData = new MutableLiveData<>();
    private List<ShoppingItem> itemList = new ArrayList<>();

    public ListViewModel() {
        totalLiveData.setValue(0.0);
        itemListLiveData.setValue(itemList);
    }

    public LiveData<Double> getTotal() {
        return totalLiveData;
    }

    public LiveData<List<ShoppingItem>> getItemList() {
        return itemListLiveData;
    }

    public void setTotal(double newTotal) {
        totalLiveData.setValue(newTotal);
    }

    public void updateItemList(List<ShoppingItem> newItemList) {
        this.itemList = newItemList;
        itemListLiveData.setValue(newItemList);
        recalculateTotal(newItemList);
    }

    public void replaceSku(String ingredientId, String newSkuName, double newPrice) {
        replaceSkuFull(ingredientId, newSkuName, newPrice, null, null);
    }

    public void replaceSkuFull(String ingredientId,
                               String newSkuName,
                               double newPrice,
                               String newSkuSpec,
                               String newImageUrl) {
        List<ShoppingItem> updated = new ArrayList<>(itemList);
        for (ShoppingItem it : updated) {
            if (ingredientId.equals(it.ingredientId)) {
                if (newSkuName != null) it.selectedSkuName = newSkuName;
                it.unitPrice = newPrice;
                if (newSkuSpec != null) it.skuSpec = newSkuSpec;
                if (newImageUrl != null && !newImageUrl.isEmpty()) it.imageUrl = newImageUrl;
                break;
            }
        }
        updateItemList(updated);
    }

    private void recalculateTotal(List<ShoppingItem> updatedList) {
        double sum = 0.0;
        for (ShoppingItem item : updatedList) {
            sum += item.unitPrice * item.quantity;
        }
        setTotal(sum);
    }

    public void recalculateQuantityForItem(String ingredientId) {
        List<ShoppingItem> updated = new ArrayList<>(itemList);
        for (ShoppingItem item : updated) {
            if (ingredientId.equals(item.ingredientId)) {
                QuantityParser.ParsedQuantity packageParsed =
                        QuantityParser.parse(item.skuSpec);
                if (packageParsed.success && item.recipeNeededValue > 0) {
                    boolean unitMatch = item.recipeNeededUnit.isEmpty() ||
                            packageParsed.unit.isEmpty() ||
                            item.recipeNeededUnit.equalsIgnoreCase(packageParsed.unit);
                    if (unitMatch) {
                        item.quantity = QuantityParser.calculatePackageCount(
                                item.recipeNeededValue,
                                packageParsed.value
                        );
                    } else {
                        item.quantity = 1;
                    }
                } else {
                    item.quantity = 1;
                }
                break;
            }
        }
        updateItemList(updated);
    }

    public void recalculateTotalOnly() {
        List<ShoppingItem> currentList = itemListLiveData.getValue();
        if (currentList != null) {
            recalculateTotal(currentList);
        }
    }

    public boolean replaceIngredient(String originalIngredientId,
                                     String substituteIngredientId,
                                     String substituteName,
                                     double quantityRatio,
                                     ShoppingItem presetData) {

        List<ShoppingItem> updated = new ArrayList<>(itemList);
        boolean found = false;

        for (ShoppingItem item : updated) {
            if (originalIngredientId.equals(item.ingredientId)) {
                found = true;

                if (item.originalIngredientId == null) {
                    item.originalIngredientId = item.ingredientId;
                }

                item.ingredientId = substituteIngredientId;
                item.name = substituteName;
                item.selectedSkuName = presetData.selectedSkuName;
                item.substituteDisplayName = "Replaced with " + substituteName;
                item.isSubstituted = true;

                item.unitPrice = presetData.unitPrice;
                item.skuSpec = presetData.skuSpec;
                item.unit = presetData.unit;
                item.aisle = presetData.aisle;

                if (presetData.imageUrl != null && !presetData.imageUrl.isEmpty()) {
                    item.imageUrl = presetData.imageUrl;
                }

                item.substitutionRatio = quantityRatio;

                item.recipeNeededValue = item.recipeNeededValue * quantityRatio;
                item.recipeNeededStr = formatQuantity(item.recipeNeededValue) +
                        (item.recipeNeededUnit.isEmpty() ? "" : " " + item.recipeNeededUnit);

                QuantityParser.ParsedQuantity packageParsed = QuantityParser.parse(item.skuSpec);
                if (packageParsed.success && item.recipeNeededValue > 0) {
                    boolean unitMatch = item.recipeNeededUnit.isEmpty() ||
                            packageParsed.unit.isEmpty() ||
                            item.recipeNeededUnit.equalsIgnoreCase(packageParsed.unit);

                    if (unitMatch) {
                        item.quantity = QuantityParser.calculatePackageCount(
                                item.recipeNeededValue,
                                packageParsed.value
                        );
                    } else {
                        item.quantity = 1;
                    }
                } else {
                    item.quantity = 1;
                }

                break;
            }
        }

        if (found) {
            updateItemList(updated);
        }

        return found;
    }

    private String formatQuantity(double value) {
        if (Math.abs(value - Math.round(value)) < 1e-9) {
            return String.valueOf((int) Math.round(value));
        }
        return String.format(java.util.Locale.US, "%.2f", value);
    }
}