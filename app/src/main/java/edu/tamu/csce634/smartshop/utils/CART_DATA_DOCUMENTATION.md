# Cart Data Storage Documentation

## Overview

The SmartShop app uses SharedPreferences to persist cart data. This allows other parts of the app and potentially other developers to access cart information.

## Storage Location

- **SharedPreferences Name**: `SmartShopCart`
- **Key**: `cart_data`
- **Format**: JSON string

## Data Structure

```json
{
  "Recipe Name 1": quantity1,
  "Recipe Name 2": quantity2,
  ...
}
```

### Example

```json
{
  "Tofu Power Bowl": 2,
  "Salmon Rice Bowl": 1,
  "Steak Taco": 3
}
```

## Accessing Cart Data

### Method 1: Using CartManager (Recommended)

```java
// Initialize (do this once in Application or MainActivity)
CartManager.getInstance(context);

// Get quantity of a specific recipe
int quantity = CartManager.getInstance(context).getQuantity("Tofu Power Bowl");

// Get all items in cart
Map<String, Integer> allItems = CartManager.getInstance(context).getAllItems();

// Get total number of items
int total = CartManager.getInstance(context).getTotalItems();

// Add recipe to cart
CartManager.getInstance(context).addRecipe("Tofu Power Bowl");

// Remove recipe from cart
CartManager.getInstance(context).removeRecipe("Tofu Power Bowl");

// Clear entire cart
CartManager.getInstance(context).clearCart();
```

### Method 2: Direct SharedPreferences Access

```java
SharedPreferences prefs = context.getSharedPreferences("SmartShopCart", Context.MODE_PRIVATE);
String cartJson = prefs.getString("cart_data", null);

if (cartJson != null) {
    Gson gson = new Gson();
    Type type = new TypeToken<HashMap<String, Integer>>(){}.getType();
    Map<String, Integer> cart = gson.fromJson(cartJson, type);
    
    // Now you can access the cart data
    for (Map.Entry<String, Integer> entry : cart.entrySet()) {
        String recipeName = entry.getKey();
        int quantity = entry.getValue();
        Log.d("Cart", recipeName + ": " + quantity);
    }
}
```

## Important Notes

1. Cart data persists across app restarts
2. Recipe names are used as keys (must match exactly)
3. Quantities are integers (0 or positive)
4. Items with quantity 0 are automatically removed from storage
5. All cart operations are thread-safe and synchronous
6. Changes are immediately persisted to SharedPreferences

## Use Cases for Other Developers

- **List Fragment**: Display cart items with quantities
- **Checkout Feature**: Access all items for order processing
- **Analytics**: Track popular recipes
- **Notifications**: Alert when cart has items
- **Widget**: Show cart count on home screen
