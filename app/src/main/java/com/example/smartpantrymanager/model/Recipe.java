package com.example.smartpantrymanager.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A recipe together with everything it requires.
 * Immutable; the ingredient list is an unmodifiable copy.
 */
public final class Recipe {

    private final long id;
    private final String name;
    private final String instructions;
    /** Preparation time in minutes. */
    private final int prepTime;
    private final List<RecipeIngredient> ingredients;

    public Recipe(long id, String name, String instructions, int prepTime,
                  List<RecipeIngredient> ingredients) {
        this.id = id;
        this.name = name;
        this.instructions = instructions;
        this.prepTime = prepTime;
        this.ingredients = ingredients == null
                ? Collections.<RecipeIngredient>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(ingredients));
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getInstructions() { return instructions; }
    public int getPrepTime() { return prepTime; }
    public List<RecipeIngredient> getIngredients() { return ingredients; }

    @Override
    public String toString() {
        return name + " (" + prepTime + " min)";
    }
}
