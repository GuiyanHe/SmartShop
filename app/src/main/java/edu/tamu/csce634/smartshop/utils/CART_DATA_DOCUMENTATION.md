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

## Aggregating Required Ingredients Across Cart

There are two ways to get a merged list of all ingredients required by the recipes currently in the cart.

### Option A (MVVM Recommended): Via `RecipeViewModel`

The `RecipeViewModel` exposes a `LiveData<Map<String, String>>` that contains the aggregated ingredients. Keys are ingredient names and values are human-readable totals (e.g., `"6 Oz"`, `"3 × 1 bag"`, or a combination when parsing is mixed).

Usage in a Fragment:

```java
RecipeViewModel viewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
viewModel.init(requireContext()); // ensure recipes are loaded and initial aggregation is computed

viewModel.getRequiredIngredients().observe(getViewLifecycleOwner(), merged -> {
  // merged is a Map<String, String> of ingredient name -> total quantity string
  StringBuilder sb = new StringBuilder();
  for (Map.Entry<String, String> e : merged.entrySet()) {
    sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
  }
  // e.g., show in a dialog or TextView
});
```

To keep the aggregation in sync, use `viewModel.addToCart(context, recipe)` and `viewModel.removeFromCart(context, recipe)`; they automatically refresh `requiredIngredients`.

### Option B: Directly via `CartManager`

The `CartManager` provides a utility method that merges ingredients across all items in the cart:

```java
// Prepare/obtain the list of Recipe objects available in the UI. The recipe title
// must match the keys stored in the cart (typically Recipe.getTitle()).
List<Recipe> allRecipes = ...; // e.g., from your ViewModel or repository

// Compute the merged ingredient list
Map<String, String> merged = CartManager.getInstance(context).getAllRequiredIngredients(allRecipes);

// Example: iterate and display
for (Map.Entry<String, String> e : merged.entrySet()) {
  String ingredientName = e.getKey();
  String totalQuantity  = e.getValue(); // e.g., "6 Oz", "3 × 1", or "2.5 L"
}
```

### Aggregation Rules (Contract)

- Input: a `List<Recipe>` containing the available recipes. Each recipe's `title` must match the names stored in the cart.
- Output: `Map<String, String>` where keys are ingredient names and values are total quantities.
- If an ingredient quantity is numeric with an optional unit (e.g., `"2 Oz"`, `"0.5 Oz"`, or `"1"`), values are multiplied by the cart quantity and summed per ingredient name (unit-aware).
- If parsing fails (e.g., complex text like `"1 bag"`), the output uses a fallback like `"3 × 1 bag"` and may combine with numeric totals as a note.
- Units are preserved in a best-effort manner. Mixed units for the same ingredient are not auto-converted; the first non-empty unit is preferred.

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
