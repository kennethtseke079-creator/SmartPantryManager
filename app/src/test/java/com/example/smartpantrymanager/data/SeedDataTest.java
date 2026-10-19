package com.example.smartpantrymanager.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.smartpantrymanager.logic.IngredientNames;
import com.example.smartpantrymanager.logic.UnitConverter;

import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Catches typos in the seed data before the app ever runs. */
public class SeedDataTest {

    @Test
    public void atLeastFifteenRecipes() {
        assertTrue(SeedData.recipes().size() >= 15);
    }

    @Test
    public void recipeNamesAreUnique() {
        Set<String> names = new HashSet<>();
        for (SeedData.RecipeDef r : SeedData.recipes()) {
            assertTrue("duplicate recipe " + r.name, names.add(r.name.toLowerCase(Locale.ROOT)));
        }
    }

    @Test
    public void ingredientNamesAreUnique() {
        Set<String> names = new HashSet<>();
        for (SeedData.IngredientDef i : SeedData.ingredients()) {
            assertTrue("duplicate ingredient " + i.name,
                    names.add(i.name.toLowerCase(Locale.ROOT)));
        }
    }

    /** Two seeded names that differ only by plural would be merged by the app. */
    @Test
    public void ingredientKeysAreUnique() {
        Set<String> keys = new HashSet<>();
        for (SeedData.IngredientDef def : SeedData.ingredients()) {
            assertTrue("clash on " + def.name, keys.add(IngredientNames.key(def.name)));
        }
    }

    @Test
    public void everyRecipeLineIsValid() {
        Map<String, String> unitByName = new HashMap<>();
        for (SeedData.IngredientDef i : SeedData.ingredients()) {
            unitByName.put(i.name.toLowerCase(Locale.ROOT), i.unit);
        }

        for (SeedData.RecipeDef r : SeedData.recipes()) {
            assertFalse(r.name + " has no ingredients", r.lines.isEmpty());
            assertFalse(r.name + " has no instructions", r.instructions.trim().isEmpty());
            assertTrue(r.name + " prep time", r.prepTimeMinutes > 0);

            Set<String> used = new HashSet<>();
            for (SeedData.Line line : r.lines) {
                String key = line.ingredientName.toLowerCase(Locale.ROOT);
                String stockUnit = unitByName.get(key);
                assertTrue(r.name + ": unknown ingredient " + line.ingredientName,
                        stockUnit != null);
                assertTrue(r.name + ": duplicate line " + line.ingredientName, used.add(key));
                assertTrue(r.name + ": quantity must be > 0", line.quantity > 0);
                assertTrue(r.name + ": unit '" + line.unit + "' not comparable with stock unit '"
                                + stockUnit + "' for " + line.ingredientName,
                        UnitConverter.areCompatible(line.unit, stockUnit));
            }
        }
    }

    @Test
    public void everyCatalogueIngredientIsUsedByARecipe() {
        Set<String> used = new HashSet<>();
        for (SeedData.RecipeDef r : SeedData.recipes()) {
            for (SeedData.Line line : r.lines) {
                used.add(line.ingredientName.toLowerCase(Locale.ROOT));
            }
        }
        for (SeedData.IngredientDef i : SeedData.ingredients()) {
            assertTrue(i.name + " is never used", used.contains(i.name.toLowerCase(Locale.ROOT)));
        }
        assertEquals(SeedData.ingredients().size(), used.size());
    }
}
