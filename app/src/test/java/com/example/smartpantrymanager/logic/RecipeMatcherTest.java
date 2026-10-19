package com.example.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantrymanager.model.Ingredient;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Plain JVM tests (no emulator needed). Run: right-click > Run in Android Studio. */
public class RecipeMatcherTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 17);
    private final RecipeMatcher matcher = new RecipeMatcher(true, TODAY);

    // ids: 1 = Eggs (piece), 2 = Flour (g), 3 = Milk (ml)
    private static Ingredient stock(long id, String name, double qty, String unit, String exp) {
        return new Ingredient(id, name, qty, unit, exp);
    }

    private static RecipeIngredient need(long ingId, String name, double qty, String unit) {
        return new RecipeIngredient(1, ingId, name, qty, unit);
    }

    private static Recipe pancakes() {
        return new Recipe(1, "Pancakes", "mix and fry", 15, Arrays.asList(
                need(1, "Eggs", 2, "piece"),
                need(2, "Flour", 100, "g"),
                need(3, "Milk", 150, "ml")));
    }

    private static List<Ingredient> fullPantry() {
        return Arrays.asList(
                stock(1, "Eggs", 2, "piece", null),
                stock(2, "Flour", 100, "g", null),
                stock(3, "Milk", 150, "ml", null));
    }

    @Test
    public void exactQuantitiesAreEnough() {
        assertTrue(matcher.canCook(pancakes(), fullPantry()));
    }

    @Test
    public void oneUnitShortRejectsTheWholeRecipe() {
        List<Ingredient> pantry = Arrays.asList(
                stock(1, "Eggs", 1, "piece", null), // needs 2
                stock(2, "Flour", 500, "g", null),
                stock(3, "Milk", 500, "ml", null));
        assertFalse(matcher.canCook(pancakes(), pantry));
    }

    @Test
    public void missingIngredientRejectsRecipe() {
        List<Ingredient> pantry = Arrays.asList(
                stock(1, "Eggs", 6, "piece", null),
                stock(2, "Flour", 500, "g", null));
        assertFalse(matcher.canCook(pancakes(), pantry));
    }

    @Test
    public void zeroStockCountsAsMissing() {
        List<Ingredient> pantry = Arrays.asList(
                stock(1, "Eggs", 6, "piece", null),
                stock(2, "Flour", 500, "g", null),
                stock(3, "Milk", 0, "ml", null));
        assertFalse(matcher.canCook(pancakes(), pantry));
    }

    @Test
    public void unitsAreConverted() {
        List<Ingredient> pantry = Arrays.asList(
                stock(1, "Eggs", 2, "piece", null),
                stock(2, "Flour", 0.1, "kg", null),  // = 100 g
                stock(3, "Milk", 0.15, "l", null));  // = 150 ml
        assertTrue(matcher.canCook(pancakes(), pantry));
    }

    @Test
    public void incompatibleUnitsNeverMatch() {
        List<Ingredient> pantry = Arrays.asList(
                stock(1, "Eggs", 2, "piece", null),
                stock(2, "Flour", 5000, "ml", null), // volume vs required mass
                stock(3, "Milk", 150, "ml", null));
        assertFalse(matcher.canCook(pancakes(), pantry));
    }

    @Test
    public void sliceIsNotPiece() {
        Recipe toast = new Recipe(2, "Toast", "", 5,
                Arrays.asList(need(9, "Bread", 2, "slice")));
        assertFalse(matcher.canCook(toast,
                Arrays.asList(stock(9, "Bread", 10, "piece", null))));
        assertTrue(matcher.canCook(toast,
                Arrays.asList(stock(9, "Bread", 2, "slices", null))));
    }

    @Test
    public void expiredStockIsIgnoredByDefault() {
        List<Ingredient> pantry = Arrays.asList(
                stock(1, "Eggs", 2, "piece", "2026-10-16"), // expired yesterday
                stock(2, "Flour", 100, "g", null),
                stock(3, "Milk", 150, "ml", null));
        assertFalse(matcher.canCook(pancakes(), pantry));
        // Expiring today is still fine.
        pantry.set(0, stock(1, "Eggs", 2, "piece", "2026-10-17"));
        assertTrue(matcher.canCook(pancakes(), pantry));
        // And the check can be turned off.
        pantry.set(0, stock(1, "Eggs", 2, "piece", "2020-01-01"));
        assertTrue(new RecipeMatcher(false, TODAY).canCook(pancakes(), pantry));
    }

    @Test
    public void floatingPointSumsDoNotCauseFalseMisses() {
        Recipe r = new Recipe(3, "R", "", 1,
                Arrays.asList(need(2, "Flour", 0.3, "kg")));
        assertTrue(matcher.canCook(r,
                Arrays.asList(stock(2, "Flour", 0.1 + 0.2, "kg", null))));
    }

    @Test
    public void recipeWithoutIngredientsIsNeverSuggested() {
        Recipe empty = new Recipe(4, "Nothing", "", 1, new ArrayList<RecipeIngredient>());
        assertFalse(matcher.canCook(empty, fullPantry()));
    }

    @Test
    public void findCookableReturnsOnlyFullMatchesSortedByTime() {
        Recipe slow = new Recipe(10, "Slow", "", 40, Arrays.asList(need(1, "Eggs", 1, "piece")));
        Recipe quick = new Recipe(11, "Quick", "", 5, Arrays.asList(need(1, "Eggs", 1, "piece")));
        Recipe impossible = new Recipe(12, "Impossible", "", 1,
                Arrays.asList(need(1, "Eggs", 1, "piece"), need(99, "Truffle", 1, "g")));

        List<Recipe> result = matcher.findCookableRecipes(fullPantry(),
                Arrays.asList(slow, impossible, quick));

        assertEquals(2, result.size());
        assertEquals("Quick", result.get(0).getName());
        assertEquals("Slow", result.get(1).getName());
    }

    @Test
    public void missingIngredientsAreReported() {
        List<Ingredient> pantry = Arrays.asList(
                stock(1, "Eggs", 2, "piece", null),
                stock(2, "Flour", 40, "g", null)); // short, and no milk
        List<RecipeIngredient> missing = matcher.getMissingIngredients(pancakes(), pantry);
        assertEquals(2, missing.size());
    }

    @Test
    public void unitConverterBasics() {
        assertEquals(1000.0, UnitConverter.convert(1, "kg", "g"), 1e-9);
        assertEquals(15.0, UnitConverter.convert(1, "tbsp", "ml"), 1e-9);
        assertTrue(Double.isNaN(UnitConverter.convert(1, "g", "ml")));
        assertEquals("g", UnitConverter.canonicalUnit(" Grams "));
        assertEquals("piece", UnitConverter.canonicalUnit("pcs"));
    }
}
