package com.example.smartpantrymanager.model;

/**
 * One requirement of a recipe: one row of the {@code recipe_ingredients} table,
 * joined with the ingredient's name for display.
 */
public final class RecipeIngredient {

    private final long recipeId;
    private final long ingredientId;
    private final String ingredientName;
    private final double requiredQuantity;
    private final String unit;

    public RecipeIngredient(long recipeId, long ingredientId, String ingredientName,
                            double requiredQuantity, String unit) {
        this.recipeId = recipeId;
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.requiredQuantity = requiredQuantity;
        this.unit = unit;
    }

    public long getRecipeId() { return recipeId; }
    public long getIngredientId() { return ingredientId; }
    public String getIngredientName() { return ingredientName; }
    public double getRequiredQuantity() { return requiredQuantity; }
    public String getUnit() { return unit; }

    @Override
    public String toString() {
        return requiredQuantity + " " + unit + " " + ingredientName;
    }
}
