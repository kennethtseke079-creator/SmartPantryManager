package com.example.smartpantrymanager.logic;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Unit handling for the strict matcher. Pure Java (no Android classes) so it can
 * be unit-tested on the JVM.
 *
 * <p>Units belong to a <em>dimension</em>: mass (g, kg, oz, lb), volume (ml, l,
 * tsp, tbsp, cup) or count. Quantities can only be compared when both units are
 * in the same dimension, e.g. 1 kg is 1000 g, but 100 g is never comparable to
 * 100 ml. Every count unit ("piece", "slice", "clove", or any custom word) is its
 * own dimension, so 2 slices never satisfies 2 pieces.</p>
 */
public final class UnitConverter {

    private static final String MASS = "mass";
    private static final String VOLUME = "volume";

    /** canonical name, dimension, and how many base units (g / ml / 1) it equals. */
    private static final class Def {
        final String canonical;
        final String dimension;
        final double factor;

        Def(String canonical, String dimension, double factor) {
            this.canonical = canonical;
            this.dimension = dimension;
            this.factor = factor;
        }
    }

    private static final Map<String, Def> UNITS = new HashMap<>();

    private static void register(String canonical, String dimension,
                                 double factor, String... aliases) {
        Def def = new Def(canonical, dimension, factor);
        UNITS.put(canonical, def);
        for (String alias : aliases) {
            UNITS.put(alias, def);
        }
    }

    static {
        // Mass (base unit: gram)
        register("g", MASS, 1.0, "gram", "grams", "gm");
        register("kg", MASS, 1000.0, "kilogram", "kilograms", "kgs");
        register("oz", MASS, 28.349523125, "ounce", "ounces");
        register("lb", MASS, 453.59237, "lbs", "pound", "pounds");

        // Volume (base unit: millilitre)
        register("ml", VOLUME, 1.0, "milliliter", "milliliters",
                "millilitre", "millilitres");
        register("l", VOLUME, 1000.0, "liter", "liters", "litre", "litres");
        register("tsp", VOLUME, 5.0, "teaspoon", "teaspoons");
        register("tbsp", VOLUME, 15.0, "tablespoon", "tablespoons");
        register("cup", VOLUME, 240.0, "cups");

        // Counts. Each is its own dimension (dimension == canonical name).
        register("piece", "count:piece", 1.0, "pieces", "pc", "pcs", "whole");
        register("slice", "count:slice", 1.0, "slices");
        register("clove", "count:clove", 1.0, "cloves");
    }

    private UnitConverter() { }

    private static Def lookup(String unit) {
        String key = unit == null ? "" : unit.trim().toLowerCase(Locale.ROOT);
        if (key.endsWith(".")) {
            key = key.substring(0, key.length() - 1);
        }
        Def known = UNITS.get(key);
        if (known != null) {
            return known;
        }
        // Unknown/custom unit (e.g. "can", "bunch"): its own count dimension.
        // A trailing plural "s" is dropped so "cans" and "can" are the same unit.
        String singular = key.length() > 3 && key.endsWith("s")
                ? key.substring(0, key.length() - 1) : key;
        return new Def(singular, "count:" + singular, 1.0);
    }

    /** Lower-case, alias-resolved unit name, e.g. "Grams" becomes "g". */
    public static String canonicalUnit(String unit) {
        return lookup(unit).canonical;
    }

    /** True if both units measure the same dimension, so they can be compared. */
    public static boolean areCompatible(String unitA, String unitB) {
        return lookup(unitA).dimension.equals(lookup(unitB).dimension);
    }

    /** Converts a quantity to its dimension's base unit (g, ml or 1). */
    public static double toBase(double quantity, String unit) {
        return quantity * lookup(unit).factor;
    }

    /**
     * Converts a quantity from one unit to another.
     *
     * @return the converted quantity, or {@link Double#NaN} if the units are
     *         not compatible
     */
    public static double convert(double quantity, String fromUnit, String toUnit) {
        Def from = lookup(fromUnit);
        Def to = lookup(toUnit);
        if (!from.dimension.equals(to.dimension)) {
            return Double.NaN;
        }
        return quantity * from.factor / to.factor;
    }
}
