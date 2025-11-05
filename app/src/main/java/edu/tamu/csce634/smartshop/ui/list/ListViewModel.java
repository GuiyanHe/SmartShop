package edu.tamu.csce634.smartshop.ui.list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.csce634.smartshop.models.ShoppingItem;

// ViewModel 层：负责保存购物数据、计算总价
public class ListViewModel extends ViewModel {

    private final MutableLiveData<Double> totalLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ShoppingItem>> itemListLiveData = new MutableLiveData<>();
    private List<ShoppingItem> itemList = new ArrayList<>();

    public ListViewModel() {
        totalLiveData.setValue(0.0);
        itemListLiveData.setValue(itemList);
    }

    // 提供 LiveData 给界面观察
    public LiveData<Double> getTotal() {
        return totalLiveData;
    }

    public LiveData<List<ShoppingItem>> getItemList() {
        return itemListLiveData;
    }

    // 更新总价
    public void setTotal(double newTotal) {
        totalLiveData.setValue(newTotal);
    }

    // 更新购物项列表并重新计算总价
    public void updateItemList(List<ShoppingItem> newItemList) {
        this.itemList = newItemList;
        itemListLiveData.setValue(newItemList);
        recalculateTotal(newItemList);
    }

    // 替换商品 SKU
    public void replaceSku(String ingredientId, String newSkuName, double newPrice) {
        // 兼容旧签名：只改名+价，其他不动
        replaceSkuFull(ingredientId, newSkuName, newPrice, null, null);
    }

    /** 新签名：一次性更新 名称 / 价格 / 规格 / 图片 */
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



    // 计算总价
    private void recalculateTotal(List<ShoppingItem> updatedList) {
        double sum = 0.0;
        for (ShoppingItem item : updatedList) {
            sum += item.unitPrice * item.quantity;
        }
        setTotal(sum);
    }
}
