package com.example.smartpantrymanager.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Hard-coded starter data: the ingredient catalogue and 19 recipes.
 * Pure Java (no Android classes) so it can be validated by a JVM unit test.
 *
 * <p>{@link DatabaseHelper} inserts all of this the first time the database is
 * created. Recipe lines refer to ingredients by NAME; the helper resolves names
 * to {@code ingredients.id} while inserting.</p>
 */
public final class SeedData {

    /** An ingredient in the catalogue and the unit its stock is counted in. */
    public static final class IngredientDef {
        public final String name;
        public final String unit;

        IngredientDef(String name, String unit) {
            this.name = name;
            this.unit = unit;
        }
    }

    /** One required ingredient line of a recipe. */
    public static final class Line {
        public final String ingredientName;
        public final double quantity;
        public final String unit;

        Line(String ingredientName, double quantity, String unit) {
            this.ingredientName = ingredientName;
            this.quantity = quantity;
            this.unit = unit;
        }
    }

    public static final class RecipeDef {
        public final String name;
        public final String instructions;
        public final int prepTimeMinutes;
        public final List<Line> lines;

        RecipeDef(String name, int prepTimeMinutes, String instructions, Line... lines) {
            this.name = name;
            this.prepTimeMinutes = prepTimeMinutes;
            this.instructions = instructions;
            this.lines = Collections.unmodifiableList(Arrays.asList(lines));
        }
    }

    private SeedData() { }

    private static IngredientDef i(String name, String unit) {
        return new IngredientDef(name, unit);
    }

    private static Line l(String name, double quantity, String unit) {
        return new Line(name, quantity, unit);
    }

    // ------------------------------------------------------------------
    // Ingredient catalogue (stock starts at 0 for all of these)
    // ------------------------------------------------------------------
    public static List<IngredientDef> ingredients() {
        return Collections.unmodifiableList(Arrays.asList(
                i("Eggs", "piece"),
                i("Butter", "g"),
                i("Salt", "g"),
                i("Black Pepper", "g"),
                i("Pasta", "g"),
                i("Tomatoes", "piece"),
                i("Garlic", "clove"),
                i("Olive Oil", "ml"),
                i("Onion", "piece"),
                i("Rice", "g"),
                i("Carrots", "piece"),
                i("Peas", "g"),
                i("Soy Sauce", "ml"),
                i("Cooking Oil", "ml"),
                i("Cheese", "g"),
                i("Bananas", "piece"),
                i("Flour", "g"),
                i("Milk", "ml"),
                i("Baking Powder", "g"),
                i("Sugar", "g"),
                i("Bread", "slice"),
                i("Potatoes", "piece"),
                i("Vegetable Stock", "ml"),
                i("Chicken Stock", "ml"),
                i("Peanut Butter", "g"),
                i("Chicken Breast", "piece"),
                i("Spinach", "g"),
                i("Oats", "g"),
                i("Apples", "piece"),
                i("Honey", "ml"),
                i("Beans", "g"),
                i("Tortillas", "piece"),
                i("Lemons", "piece")
        ));
    }

    // ------------------------------------------------------------------
    // Recipes (19)
    // ------------------------------------------------------------------
    public static List<RecipeDef> recipes() {
        List<RecipeDef> r = new ArrayList<>();

        r.add(new RecipeDef("Scrambled Eggs", 10,
                "1. Beat the eggs with salt and pepper.\n"
                        + "2. Melt the butter in a pan over low heat.\n"
                        + "3. Pour in the eggs and stir gently until just set.",
                l("Eggs", 3, "piece"), l("Butter", 10, "g"),
                l("Salt", 1, "g"), l("Black Pepper", 0.5, "g")));

        r.add(new RecipeDef("Cheese Omelette", 12,
                "1. Beat the eggs with salt.\n"
                        + "2. Melt the butter in a pan and pour in the eggs.\n"
                        + "3. When almost set, add the grated cheese and fold over.",
                l("Eggs", 3, "piece"), l("Cheese", 40, "g"),
                l("Butter", 10, "g"), l("Salt", 1, "g")));

        r.add(new RecipeDef("Tomato Pasta", 25,
                "1. Boil the pasta in salted water until al dente.\n"
                        + "2. Fry the crushed garlic in olive oil, add chopped tomatoes.\n"
                        + "3. Simmer 10 minutes, then toss with the drained pasta.",
                l("Pasta", 200, "g"), l("Tomatoes", 3, "piece"),
                l("Garlic", 2, "clove"), l("Olive Oil", 15, "ml"),
                l("Salt", 2, "g")));

        r.add(new RecipeDef("Vegetable Fried Rice", 20,
                "1. Heat the oil and stir-fry diced carrot and peas for 3 minutes.\n"
                        + "2. Push aside, scramble the eggs in the pan.\n"
                        + "3. Add cooked rice and soy sauce and fry until hot.",
                l("Rice", 200, "g"), l("Eggs", 2, "piece"),
                l("Carrots", 1, "piece"), l("Peas", 50, "g"),
                l("Soy Sauce", 20, "ml"), l("Cooking Oil", 15, "ml")));

        r.add(new RecipeDef("Banana Pancakes", 20,
                "1. Mash the bananas and whisk in the eggs and milk.\n"
                        + "2. Stir in flour and baking powder to a thick batter.\n"
                        + "3. Fry spoonfuls in melted butter until golden on both sides.",
                l("Bananas", 2, "piece"), l("Eggs", 2, "piece"),
                l("Flour", 120, "g"), l("Milk", 150, "ml"),
                l("Baking Powder", 5, "g"), l("Butter", 10, "g")));

        r.add(new RecipeDef("Garlic Bread", 15,
                "1. Mix softened butter with crushed garlic and salt.\n"
                        + "2. Spread on the bread slices.\n"
                        + "3. Bake at 200 C for 8 to 10 minutes until golden.",
                l("Bread", 4, "slice"), l("Butter", 40, "g"),
                l("Garlic", 3, "clove"), l("Salt", 1, "g")));

        r.add(new RecipeDef("Mashed Potatoes", 30,
                "1. Peel and chop the potatoes, boil in salted water for 15 to 20 minutes.\n"
                        + "2. Drain and mash with butter and warm milk.\n"
                        + "3. Season with salt.",
                l("Potatoes", 4, "piece"), l("Butter", 30, "g"),
                l("Milk", 60, "ml"), l("Salt", 2, "g")));

        r.add(new RecipeDef("Carrot Soup", 35,
                "1. Soften the chopped onion in olive oil.\n"
                        + "2. Add sliced carrots and the stock, simmer 20 minutes.\n"
                        + "3. Blend until smooth and season with salt.",
                l("Carrots", 4, "piece"), l("Onion", 1, "piece"),
                l("Vegetable Stock", 750, "ml"), l("Olive Oil", 15, "ml"),
                l("Salt", 2, "g")));

        r.add(new RecipeDef("Peanut Butter Sandwich", 5,
                "1. Spread peanut butter on one slice of bread.\n"
                        + "2. Top with the second slice and cut in half.",
                l("Bread", 2, "slice"), l("Peanut Butter", 30, "g")));

        r.add(new RecipeDef("Tomato Onion Salad", 10,
                "1. Slice the tomatoes and onion thinly.\n"
                        + "2. Dress with olive oil and salt and toss.",
                l("Tomatoes", 3, "piece"), l("Onion", 1, "piece"),
                l("Olive Oil", 15, "ml"), l("Salt", 1, "g")));

        r.add(new RecipeDef("Chicken and Rice", 45,
                "1. Brown the diced chicken in oil, add chopped onion and garlic.\n"
                        + "2. Stir in the rice, then pour in the stock.\n"
                        + "3. Cover and simmer 20 minutes until the rice is tender.",
                l("Chicken Breast", 2, "piece"), l("Rice", 200, "g"),
                l("Onion", 1, "piece"), l("Garlic", 2, "clove"),
                l("Chicken Stock", 500, "ml"), l("Cooking Oil", 15, "ml"),
                l("Salt", 2, "g")));

        r.add(new RecipeDef("Spinach Scramble", 15,
                "1. Sauté the garlic in oil, add spinach until wilted.\n"
                        + "2. Pour in the beaten eggs and stir until set.\n"
                        + "3. Season with salt.",
                l("Spinach", 100, "g"), l("Eggs", 3, "piece"),
                l("Garlic", 1, "clove"), l("Cooking Oil", 10, "ml"),
                l("Salt", 1, "g")));

        r.add(new RecipeDef("Apple Oatmeal", 10,
                "1. Simmer the oats in milk for 5 minutes, stirring.\n"
                        + "2. Top with chopped apple and a drizzle of honey.",
                l("Oats", 60, "g"), l("Milk", 250, "ml"),
                l("Apples", 1, "piece"), l("Honey", 1, "tbsp")));

        r.add(new RecipeDef("Bean and Tomato Stew", 35,
                "1. Soften onion and garlic in oil.\n"
                        + "2. Add chopped tomatoes and beans, simmer 20 minutes.\n"
                        + "3. Season with salt.",
                l("Beans", 400, "g"), l("Tomatoes", 3, "piece"),
                l("Onion", 1, "piece"), l("Garlic", 2, "clove"),
                l("Cooking Oil", 15, "ml"), l("Salt", 2, "g")));

        r.add(new RecipeDef("Cheese Quesadilla", 12,
                "1. Butter one side of a tortilla and place it in a pan.\n"
                        + "2. Cover with cheese and the second tortilla.\n"
                        + "3. Cook until crisp and melted, flipping once.",
                l("Tortillas", 2, "piece"), l("Cheese", 100, "g"),
                l("Butter", 5, "g")));

        r.add(new RecipeDef("French Toast", 15,
                "1. Whisk eggs, milk and sugar in a shallow dish.\n"
                        + "2. Soak the bread slices for 30 seconds per side.\n"
                        + "3. Fry in butter until golden brown.",
                l("Bread", 4, "slice"), l("Eggs", 2, "piece"),
                l("Milk", 100, "ml"), l("Sugar", 10, "g"),
                l("Butter", 15, "g")));

        r.add(new RecipeDef("Roast Potato Wedges", 40,
                "1. Cut the potatoes into wedges and toss with oil, salt and pepper.\n"
                        + "2. Spread on a tray and roast at 220 C for 30 minutes, turning once.",
                l("Potatoes", 4, "piece"), l("Olive Oil", 30, "ml"),
                l("Salt", 3, "g"), l("Black Pepper", 1, "g")));

        r.add(new RecipeDef("Lemon Garlic Chicken", 30,
                "1. Season the chicken with salt and pepper.\n"
                        + "2. Sear in olive oil 6 minutes per side.\n"
                        + "3. Add crushed garlic and lemon juice, cook 2 more minutes.",
                l("Chicken Breast", 2, "piece"), l("Lemons", 1, "piece"),
                l("Garlic", 3, "clove"), l("Olive Oil", 30, "ml"),
                l("Salt", 2, "g"), l("Black Pepper", 1, "g")));

        r.add(new RecipeDef("Cheesy Pasta", 20,
                "1. Boil the pasta until al dente and drain.\n"
                        + "2. Melt butter with milk, stir in the grated cheese until smooth.\n"
                        + "3. Toss the pasta in the sauce and season with salt.",
                l("Pasta", 200, "g"), l("Cheese", 80, "g"),
                l("Butter", 20, "g"), l("Milk", 100, "ml"),
                l("Salt", 1, "g")));

        return Collections.unmodifiableList(r);
    }
}
