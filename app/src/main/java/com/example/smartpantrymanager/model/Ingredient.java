package com.example.smartpantrymanager.model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * A pantry ingredient: one row of the {@code ingredients} table.
 *
 * <p>The table doubles as the ingredient catalogue (every ingredient any recipe
 * can use exists as a row) and as the user's stock: {@code quantity == 0} means
 * "known ingredient, none in the pantry right now".</p>
 *
 * <p>Instances are immutable. To change stock, build a new Ingredient and pass it
 * to {@code DatabaseHelper.updateIngredient()}.</p>
 */
public final class Ingredient {

    private final long id;
    private final String name;
    private final double quantity;
    private final String unit;
    /** ISO date "yyyy-MM-dd", or null when the item has no expiry date. */
    private final String expiryDate;

    public Ingredient(long id, String name, double quantity,
                      String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    /** Convenience constructor for a not-yet-saved ingredient (id = 0). */
    public Ingredient(String name, double quantity, String unit, String expiryDate) {
        this(0, name, quantity, unit, expiryDate);
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiryDate() { return expiryDate; }

    /**
     * Parses a "yyyy-MM-dd" string.
     *
     * @return the date, or null if the text is null, blank or not a valid date
     */
    public static LocalDate parseDate(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * An item is expired when its expiry date is strictly before {@code today}.
     * Items without a (valid) expiry date never expire.
     */
    public boolean isExpired(LocalDate today) {
        LocalDate expiry = parseDate(expiryDate);
        return expiry != null && expiry.isBefore(today);
    }

    @Override
    public String toString() {
        return name + " (" + quantity + " " + unit
                + (expiryDate != null ? ", exp " + expiryDate : "") + ")";
    }
}
