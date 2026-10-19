package com.example.smartpantrymanager.logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Turns ingredient names into a comparable "key" so trivial differences do not
 * break matching: "Tomato", "tomatoes" and " TOMATOES " all give the key "tomato".
 *
 * <p>Pure Java (no Android classes), so it is unit-testable on the JVM.
 * This is deliberately a small rule-based singulariser, not a full NLP solution.</p>
 */
public final class IngredientNames {

    /** Words that end in s/us/is but are NOT plurals (or are always plural). */
    private static final Set<String> KEEP_AS_IS = new HashSet<>(Arrays.asList(
            "hummus", "couscous", "asparagus", "molasses", "swiss", "citrus",
            "lemongrass", "watercress", "bass", "gas"));

    private IngredientNames() { }

    /**
     * Normalised comparison key: lower case, punctuation removed, whitespace
     * collapsed and every word reduced to its singular form.
     * Returns "" for null/blank input.
     */
    public static String key(String name) {
        if (name == null) {
            return "";
        }
        String cleaned = name.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{Nd}]+", " ")
                .trim();
        if (cleaned.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        for (String word : cleaned.split(" ")) {
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(singular(word));
        }
        return out.toString();
    }

    /** True if both names refer to the same ingredient (ignoring case/plurals). */
    public static boolean sameIngredient(String a, String b) {
        String keyA = key(a);
        return !keyA.isEmpty() && keyA.equals(key(b));
    }

    private static String singular(String word) {
        int n = word.length();
        if (n <= 3 || KEEP_AS_IS.contains(word)) {
            return word;
        }
        if (word.endsWith("ies") && n > 4) {          // berries -> berry
            return word.substring(0, n - 3) + "y";
        }
        if (word.endsWith("oes")) {                    // tomatoes -> tomato
            return word.substring(0, n - 2);
        }
        if (word.endsWith("ches") || word.endsWith("shes")
                || word.endsWith("sses") || word.endsWith("xes")
                || word.endsWith("zes")) {             // peaches -> peach
            return word.substring(0, n - 2);
        }
        if (word.endsWith("ss") || word.endsWith("us") || word.endsWith("is")) {
            return word;                               // glass, hummus, basis
        }
        if (word.endsWith("s")) {                      // eggs -> egg
            return word.substring(0, n - 1);
        }
        return word;
    }
}
