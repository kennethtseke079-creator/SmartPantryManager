package com.example.smartpantrymanager.logic;

import com.example.smartpantrymanager.model.Ingredient;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Strict recipe matching engine. Pure Java (no Android classes), so it is
 * unit-testable on the JVM.
 *
 * <p><b>A recipe is suggested only if EVERY one of its ingredients is covered:</b></p>
 * <ol>
 *   <li>the ingredient exists in the pantry (matched by ingredient id);</li>
 *   <li>its unit is compatible with the recipe's unit (g with kg is fine,
 *       g with ml is not, which counts as NOT satisfied);</li>
 *   <li>the stock, converted to the recipe's unit, is at least the required
 *       quantity;</li>
 *   <li>the stock is not expired (can be switched off in the constructor).</li>
 * </ol>
 * <p>A recipe that lists no ingredients is never suggested.</p>
 */
public class RecipeMatcher {

    /** Tolerance so 0.1 + 0.2 style floating-point sums don't cause false misses. */
    private static final double EPSILON = 1e-9;

    private final boolean excludeExpired;
    private final LocalDate today;

    /** Default: expired stock is ignored, "today" is the device date. */
    public RecipeMatcher() {
        this(true, LocalDate.now());
    }

    /**
     * @param excludeExpired if true, pantry items past their expiry date do not count
     * @param today          the date used for expiry checks (injectable for tests)
     */
    public RecipeMatcher(boolean excludeExpired, LocalDate today) {
        this.excludeExpired = excludeExpired;
        this.today = today;
    }

    /**
     * Returns only the recipes that can be cooked right now, quickest first
     * (ties broken alphabetically).
     */
    public List<Recipe> findCookableRecipes(List<Ingredient> pantry,
                                            List<Recipe> recipes) {
        Map<Long, Ingredient> stock = indexById(pantry);
        List<Recipe> result = new ArrayList<>();

        for (Recipe recipe : recipes) {
            if (canCook(recipe, stock)) {
                result.add(recipe);
            }
        }

        Collections.sort(result, new Comparator<Recipe>() {
            @Override
            public int compare(Recipe a, Recipe b) {
                int byTime = Integer.compare(a.getPrepTime(), b.getPrepTime());
                return byTime != 0
                        ? byTime
                        : a.getName().compareToIgnoreCase(b.getName());
            }
        });
        return result;
    }

    /** True if every ingredient of the recipe is fully covered by the pantry. */
    public boolean canCook(Recipe recipe, List<Ingredient> pantry) {
        return canCook(recipe, indexById(pantry));
    }

    /**
     * The requirements that are NOT covered: handy for a "you still need..."
     * screen. Empty list means the recipe is cookable (unless it has no ingredients).
     */
    public List<RecipeIngredient> getMissingIngredients(Recipe recipe,
                                                        List<Ingredient> pantry) {
        Map<Long, Ingredient> stock = indexById(pantry);
        List<RecipeIngredient> missing = new ArrayList<>();
        for (RecipeIngredient need : recipe.getIngredients()) {
            if (!isSatisfied(need, stock.get(need.getIngredientId()))) {
                missing.add(need);
            }
        }
        return missing;
    }

    // ------------------------------------------------------------------

    private boolean canCook(Recipe recipe, Map<Long, Ingredient> stock) {
        List<RecipeIngredient> needs = recipe.getIngredients();
        if (needs.isEmpty()) {
            return false; // nothing to verify, so never suggest
        }
        for (RecipeIngredient need : needs) {
            if (!isSatisfied(need, stock.get(need.getIngredientId()))) {
                return false; // one failure disqualifies the whole recipe
            }
        }
        return true;
    }

    private boolean isSatisfied(RecipeIngredient need, Ingredient have) {
        if (have == null) {
            return false;
        }
        if (excludeExpired && have.isExpired(today)) {
            return false;
        }
        if (!UnitConverter.areCompatible(have.getUnit(), need.getUnit())) {
            return false;
        }
        double available = UnitConverter.toBase(have.getQuantity(), have.getUnit());
        double required = UnitConverter.toBase(need.getRequiredQuantity(), need.getUnit());
        return available + EPSILON >= required;
    }

    private static Map<Long, Ingredient> indexById(List<Ingredient> pantry) {
        Map<Long, Ingredient> map = new HashMap<>();
        if (pantry != null) {
            for (Ingredient item : pantry) {
                map.put(item.getId(), item);
            }
        }
        return map;
    }
}
