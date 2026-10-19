package com.example.smartpantrymanager.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IngredientNamesTest {

    @Test
    public void singularAndPluralAreTheSameIngredient() {
        assertTrue(IngredientNames.sameIngredient("tomato", "Tomatoes"));
        assertTrue(IngredientNames.sameIngredient("Egg", "eggs"));
        assertTrue(IngredientNames.sameIngredient("potato", "POTATOES"));
        assertTrue(IngredientNames.sameIngredient("banana", "Bananas"));
        assertTrue(IngredientNames.sameIngredient("carrot", "carrots"));
        assertTrue(IngredientNames.sameIngredient("Apple", "apples"));
        assertTrue(IngredientNames.sameIngredient("lemon", "Lemons"));
        assertTrue(IngredientNames.sameIngredient("tortilla", "Tortillas"));
        assertTrue(IngredientNames.sameIngredient("chicken breast", "Chicken Breasts"));
    }

    @Test
    public void caseSpacingAndPunctuationAreIgnored() {
        assertEquals("peanut butter", IngredientNames.key("  Peanut   BUTTER "));
        assertEquals("olive oil", IngredientNames.key("Olive-Oil"));
    }

    @Test
    public void wordsThatAreNotPluralsAreKept() {
        assertEquals("hummus", IngredientNames.key("Hummus"));
        assertEquals("couscous", IngredientNames.key("Couscous"));
        assertEquals("asparagus", IngredientNames.key("Asparagus"));
        assertEquals("glass", IngredientNames.key("glass"));
    }

    @Test
    public void differentIngredientsStayDifferent() {
        assertFalse(IngredientNames.sameIngredient("Butter", "Peanut Butter"));
        assertFalse(IngredientNames.sameIngredient("Chicken Stock", "Vegetable Stock"));
        assertFalse(IngredientNames.sameIngredient("", ""));
        assertFalse(IngredientNames.sameIngredient(null, "eggs"));
    }
}
