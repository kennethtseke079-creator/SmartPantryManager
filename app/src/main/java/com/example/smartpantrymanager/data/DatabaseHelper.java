package com.example.smartpantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.logic.IngredientNames;
import com.example.smartpantrymanager.logic.UnitConverter;
import com.example.smartpantrymanager.model.Ingredient;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * SQLite storage for SmartPantryManager.
 *
 * <h3>Schema</h3>
 * <pre>
 * ingredients        (id, name UNIQUE, quantity, unit, expiry_date)
 * recipes            (id, name UNIQUE, instructions, prep_time)
 * recipe_ingredients (recipe_id, ingredient_id, required_quantity, unit)
 * </pre>
 *
 * <p>The {@code ingredients} table is both the catalogue of known ingredients and
 * the user's pantry stock: quantity 0 means "known, but none in stock". Seeded
 * recipes point at these rows by {@code ingredient_id}, so the pantry and the
 * recipes always agree on what "Eggs" means.</p>
 *
 * <p>Use {@link #getInstance(Context)}; do not create several helpers.</p>
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    /** v2 = spec schema. v1 was an early dev schema (pantry_items), see onUpgrade. */
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_INGREDIENTS = "ingredients";
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";

    /** Returned by {@link #addIngredient} when a field is invalid. */
    public static final long ERR_INVALID_INPUT = -1;
    /** Returned by {@link #addIngredient} when the ingredient exists in an incompatible unit. */
    public static final long ERR_UNIT_MISMATCH = -2;

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ==================================================================
    // Lifecycle
    // ==================================================================

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // SQLite ignores FKs unless enabled
    }

    /** Runs once, inside a transaction, the first time the database file is created. */
    @Override
    public void onCreate(SQLiteDatabase db) {
        createSchema(db);
        seed(db);
    }

    /**
     * Version 1 was an early development schema with different tables, and it held
     * no user data worth migrating, so older databases are rebuilt from scratch.
     * For future versions add real migrations here instead of dropping tables.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
            db.execSQL("DROP TABLE IF EXISTS recipes");
            db.execSQL("DROP TABLE IF EXISTS ingredients");
            db.execSQL("DROP TABLE IF EXISTS pantry_items"); // v1 leftover
            onCreate(db);
        }
    }

    private void createSchema(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_INGREDIENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL UNIQUE COLLATE NOCASE, "
                + "quantity REAL NOT NULL DEFAULT 0 CHECK(quantity >= 0), "
                + "unit TEXT NOT NULL, "
                + "expiry_date TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL UNIQUE, "
                + "instructions TEXT NOT NULL, "
                + "prep_time INTEGER NOT NULL DEFAULT 0 CHECK(prep_time >= 0))");

        // A recipe cannot list the same ingredient twice (composite PK).
        // Deleting a recipe removes its lines; an ingredient used by a recipe
        // can't be deleted (RESTRICT), see deleteIngredient().
        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " ("
                + "recipe_id INTEGER NOT NULL, "
                + "ingredient_id INTEGER NOT NULL, "
                + "required_quantity REAL NOT NULL CHECK(required_quantity > 0), "
                + "unit TEXT NOT NULL, "
                + "PRIMARY KEY (recipe_id, ingredient_id), "
                + "FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE, "
                + "FOREIGN KEY (ingredient_id) REFERENCES ingredients(id) ON DELETE RESTRICT)");

        db.execSQL("CREATE INDEX idx_recipe_ingredients_ingredient "
                + "ON " + TABLE_RECIPE_INGREDIENTS + "(ingredient_id)");
    }

    // ==================================================================
    // Seed data (see SeedData.java for the 19 recipes)
    // ==================================================================

    private void seed(SQLiteDatabase db) {
        Map<String, Long> idByName = new HashMap<>();

        for (SeedData.IngredientDef def : SeedData.ingredients()) {
            ContentValues v = new ContentValues();
            v.put("name", def.name);
            v.put("quantity", 0);
            v.put("unit", UnitConverter.canonicalUnit(def.unit));
            long id = db.insertOrThrow(TABLE_INGREDIENTS, null, v);
            idByName.put(def.name.toLowerCase(Locale.ROOT), id);
        }

        for (SeedData.RecipeDef recipe : SeedData.recipes()) {
            ContentValues rv = new ContentValues();
            rv.put("name", recipe.name);
            rv.put("instructions", recipe.instructions);
            rv.put("prep_time", recipe.prepTimeMinutes);
            long recipeId = db.insertOrThrow(TABLE_RECIPES, null, rv);

            for (SeedData.Line line : recipe.lines) {
                Long ingredientId = idByName.get(line.ingredientName.toLowerCase(Locale.ROOT));
                if (ingredientId == null) {
                    // Programming error in SeedData, fail loudly during development.
                    throw new IllegalStateException("Seed recipe '" + recipe.name
                            + "' uses unknown ingredient '" + line.ingredientName + "'");
                }
                ContentValues lv = new ContentValues();
                lv.put("recipe_id", recipeId);
                lv.put("ingredient_id", ingredientId);
                lv.put("required_quantity", line.quantity);
                lv.put("unit", UnitConverter.canonicalUnit(line.unit));
                db.insertOrThrow(TABLE_RECIPE_INGREDIENTS, null, lv);
            }
        }
    }

    // ==================================================================
    // Pantry CRUD: CREATE
    // ==================================================================

    /**
     * Adds stock to the pantry.
     *
     * <ul>
     *   <li>If the ingredient already exists, the quantity is ADDED to the existing
     *       stock, converting units when needed (500 g added to a row stored in kg
     *       works). The earlier expiry date of the two is kept. "Exists" ignores case
     *       and singular/plural: typing "tomato" finds the "Tomatoes" row, so recipes
     *       keep seeing the stock (see {@link IngredientNames}).</li>
     *   <li>Otherwise a new ingredient row is created.</li>
     * </ul>
     *
     * @param expiryDate "yyyy-MM-dd" or null/blank for none
     * @return the ingredient id, {@link #ERR_INVALID_INPUT}, or
     *         {@link #ERR_UNIT_MISMATCH} (e.g. adding ml to an ingredient kept in g)
     */
    public long addIngredient(String name, double quantity, String unit, String expiryDate) {
        if (!isValid(name, quantity, unit, expiryDate)) {
            return ERR_INVALID_INPUT;
        }
        String cleanName = name.trim();
        String cleanUnit = UnitConverter.canonicalUnit(unit);
        String cleanExpiry = cleanExpiry(expiryDate);

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            Ingredient existing = findByKey(db, cleanName, -1);
            long resultId;

            if (existing == null) {
                ContentValues v = new ContentValues();
                v.put("name", cleanName);
                v.put("quantity", quantity);
                v.put("unit", cleanUnit);
                v.put("expiry_date", cleanExpiry);
                resultId = db.insertOrThrow(TABLE_INGREDIENTS, null, v);
            } else {
                double converted = UnitConverter.convert(quantity, cleanUnit, existing.getUnit());
                if (Double.isNaN(converted)) {
                    return ERR_UNIT_MISMATCH; // finally{} ends the transaction (rolled back)
                }
                ContentValues v = new ContentValues();
                v.put("quantity", existing.getQuantity() + converted);
                v.put("expiry_date", earlierDate(existing.getExpiryDate(), cleanExpiry));
                db.update(TABLE_INGREDIENTS, v, "id = ?",
                        new String[]{String.valueOf(existing.getId())});
                resultId = existing.getId();
            }
            db.setTransactionSuccessful();
            return resultId;
        } catch (SQLException e) {
            return ERR_INVALID_INPUT;
        } finally {
            db.endTransaction();
        }
    }

    // ==================================================================
    // Pantry CRUD: READ
    // ==================================================================

    /** Every known ingredient, including ones with zero stock (for "add" pickers). */
    public List<Ingredient> getAllIngredients() {
        return queryIngredients(null, null);
    }

    /** Only what is actually in the pantry (quantity &gt; 0): the pantry screen list. */
    public List<Ingredient> getPantryItems() {
        return queryIngredients("quantity > 0", null);
    }

    /** @return the ingredient, or null if the id does not exist */
    public Ingredient getIngredient(long id) {
        List<Ingredient> list = queryIngredients("id = ?", new String[]{String.valueOf(id)});
        return list.isEmpty() ? null : list.get(0);
    }

    // ==================================================================
    // Pantry CRUD: UPDATE
    // ==================================================================

    /**
     * Overwrites name, quantity, unit and expiry of an existing ingredient with
     * the values in {@code item} (this is "set", unlike addIngredient which adds).
     *
     * @return true if a row was updated; false for invalid values, an unknown id,
     *         or a name that clashes with another ingredient
     */
    public boolean updateIngredient(Ingredient item) {
        if (item == null || item.getId() <= 0
                || !isValid(item.getName(), item.getQuantity(),
                item.getUnit(), item.getExpiryDate())) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        // Renaming must not collide with another ingredient ("Tomato" vs "Tomatoes").
        if (findByKey(db, item.getName(), item.getId()) != null) {
            return false;
        }
        ContentValues v = new ContentValues();
        v.put("name", item.getName().trim());
        v.put("quantity", item.getQuantity());
        v.put("unit", UnitConverter.canonicalUnit(item.getUnit()));
        v.put("expiry_date", cleanExpiry(item.getExpiryDate()));
        try {
            return db.update(TABLE_INGREDIENTS, v, "id = ?",
                    new String[]{String.valueOf(item.getId())}) > 0;
        } catch (SQLException e) { // e.g. UNIQUE(name) violated
            return false;
        }
    }

    // ==================================================================
    // Pantry CRUD: DELETE
    // ==================================================================

    /**
     * Removes an ingredient from the pantry.
     *
     * <p>If any recipe uses the ingredient, the row cannot be physically deleted
     * (that would silently change the recipe), so its stock is reset to 0 and its
     * expiry cleared instead. It then disappears from {@link #getPantryItems()}.
     * Ingredients no recipe uses (ones the user added) are deleted outright.</p>
     *
     * @return true if the ingredient existed and was removed or reset
     */
    public boolean deleteIngredient(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            boolean changed;
            if (isUsedByRecipe(db, id)) {
                ContentValues v = new ContentValues();
                v.put("quantity", 0);
                v.putNull("expiry_date");
                changed = db.update(TABLE_INGREDIENTS, v, "id = ?",
                        new String[]{String.valueOf(id)}) > 0;
            } else {
                changed = db.delete(TABLE_INGREDIENTS, "id = ?",
                        new String[]{String.valueOf(id)}) > 0;
            }
            db.setTransactionSuccessful();
            return changed;
        } finally {
            db.endTransaction();
        }
    }

    // ==================================================================
    // Recipes (read-only), fed to RecipeMatcher
    // ==================================================================

    /**
     * Loads all recipes with their ingredient lists using two queries total.
     * Pass the result to {@code RecipeMatcher.findCookableRecipes()}.
     */
    public List<Recipe> getAllRecipes() {
        return loadRecipes(null);
    }

    /** @return the recipe with its ingredients, or null if the id does not exist */
    public Recipe getRecipe(long id) {
        List<Recipe> list = loadRecipes(String.valueOf(id));
        return list.isEmpty() ? null : list.get(0);
    }

    /** @param onlyRecipeId restrict to one recipe id, or null for all recipes */
    private List<Recipe> loadRecipes(String onlyRecipeId) {
        SQLiteDatabase db = getReadableDatabase();
        String[] idArgs = onlyRecipeId == null ? null : new String[]{onlyRecipeId};

        Map<Long, List<RecipeIngredient>> linesByRecipe = new HashMap<>();
        String sql = "SELECT ri.recipe_id, ri.ingredient_id, i.name, "
                + "ri.required_quantity, ri.unit "
                + "FROM " + TABLE_RECIPE_INGREDIENTS + " ri "
                + "JOIN " + TABLE_INGREDIENTS + " i ON i.id = ri.ingredient_id "
                + (onlyRecipeId == null ? "" : "WHERE ri.recipe_id = ? ")
                + "ORDER BY ri.recipe_id, i.name COLLATE NOCASE";
        try (Cursor c = db.rawQuery(sql, idArgs)) {
            while (c.moveToNext()) {
                long recipeId = c.getLong(0);
                List<RecipeIngredient> list = linesByRecipe.get(recipeId);
                if (list == null) {
                    list = new ArrayList<>();
                    linesByRecipe.put(recipeId, list);
                }
                list.add(new RecipeIngredient(recipeId, c.getLong(1),
                        c.getString(2), c.getDouble(3), c.getString(4)));
            }
        }

        List<Recipe> recipes = new ArrayList<>();
        try (Cursor c = db.query(TABLE_RECIPES,
                new String[]{"id", "name", "instructions", "prep_time"},
                onlyRecipeId == null ? null : "id = ?", idArgs,
                null, null, "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                long id = c.getLong(0);
                recipes.add(new Recipe(id, c.getString(1), c.getString(2),
                        c.getInt(3), linesByRecipe.get(id)));
            }
        }
        return recipes;
    }

    // ==================================================================
    // Private helpers
    // ==================================================================

    private List<Ingredient> queryIngredients(String where, String[] args) {
        List<Ingredient> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(TABLE_INGREDIENTS,
                new String[]{"id", "name", "quantity", "unit", "expiry_date"},
                where, args, null, null, "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                list.add(readIngredient(c));
            }
        }
        return list;
    }

    /**
     * Finds an ingredient whose name matches {@code name} after normalisation
     * (case, spacing, singular/plural). The table only holds a few dozen rows,
     * so comparing in Java is simple and fast enough.
     *
     * @param excludeId an id to skip (the row being edited), or -1
     */
    private Ingredient findByKey(SQLiteDatabase db, String name, long excludeId) {
        String key = IngredientNames.key(name);
        try (Cursor c = db.query(TABLE_INGREDIENTS,
                new String[]{"id", "name", "quantity", "unit", "expiry_date"},
                null, null, null, null, null)) {
            while (c.moveToNext()) {
                Ingredient candidate = readIngredient(c);
                if (candidate.getId() != excludeId
                        && IngredientNames.key(candidate.getName()).equals(key)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static Ingredient readIngredient(Cursor c) {
        // Column order matches the projections above; expiry_date may be NULL.
        return new Ingredient(c.getLong(0), c.getString(1), c.getDouble(2),
                c.getString(3), c.isNull(4) ? null : c.getString(4));
    }

    private boolean isUsedByRecipe(SQLiteDatabase db, long ingredientId) {
        try (Cursor c = db.rawQuery("SELECT 1 FROM " + TABLE_RECIPE_INGREDIENTS
                        + " WHERE ingredient_id = ? LIMIT 1",
                new String[]{String.valueOf(ingredientId)})) {
            return c.moveToFirst();
        }
    }

    private static boolean isValid(String name, double quantity,
                                   String unit, String expiryDate) {
        if (name == null || name.trim().isEmpty()) return false;
        if (unit == null || unit.trim().isEmpty()) return false;
        if (Double.isNaN(quantity) || Double.isInfinite(quantity) || quantity < 0) return false;
        // A non-blank expiry must be a real date in yyyy-MM-dd form.
        return expiryDate == null || expiryDate.trim().isEmpty()
                || Ingredient.parseDate(expiryDate) != null;
    }

    private static String cleanExpiry(String expiryDate) {
        return expiryDate == null || expiryDate.trim().isEmpty() ? null : expiryDate.trim();
    }

    /** ISO dates sort correctly as text, so the earlier date is the smaller string. */
    private static String earlierDate(String a, String b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.compareTo(b) <= 0 ? a : b;
    }
}
