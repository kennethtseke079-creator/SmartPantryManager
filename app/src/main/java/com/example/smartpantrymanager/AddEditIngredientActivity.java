package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.logic.IngredientNames;
import com.example.smartpantrymanager.model.Ingredient;
import com.example.smartpantrymanager.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Screen 2: Add / Edit Ingredient.
 *
 * <p>Opened with an Intent. If the Intent carries {@link #EXTRA_INGREDIENT_ID} the
 * screen edits that ingredient (UPDATE); otherwise it creates a new one (CREATE).
 * Every field is validated before anything is written to the database.</p>
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    /** Intent extra: id of the ingredient to edit. Absent (or -1) means "add new". */
    public static final String EXTRA_INGREDIENT_ID = "ingredient_id";

    private static final String[] COMMON_UNITS = {
            "g", "kg", "ml", "l", "tsp", "tbsp", "cup", "piece", "slice", "clove"};

    private DatabaseHelper db;
    private Ingredient editing; // null when adding

    private TextInputLayout layoutName, layoutQuantity, layoutUnit, layoutExpiry;
    private AutoCompleteTextView inputName, inputUnit;
    private EditText inputQuantity, inputExpiry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_edit_ingredient);
        UiUtils.applySystemBarInsets(findViewById(R.id.main));

        db = DatabaseHelper.getInstance(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        layoutName = findViewById(R.id.layoutName);
        layoutQuantity = findViewById(R.id.layoutQuantity);
        layoutUnit = findViewById(R.id.layoutUnit);
        layoutExpiry = findViewById(R.id.layoutExpiry);
        inputName = findViewById(R.id.inputName);
        inputQuantity = findViewById(R.id.inputQuantity);
        inputUnit = findViewById(R.id.inputUnit);
        inputExpiry = findViewById(R.id.inputExpiry);

        setUpSuggestions();

        // Read the Intent: edit mode if an id was passed.
        long id = getIntent().getLongExtra(EXTRA_INGREDIENT_ID, -1);
        if (id > 0) {
            editing = db.getIngredient(id);
            if (editing == null) {
                Toast.makeText(this, "Ingredient not found", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            toolbar.setTitle("Edit ingredient");
            inputName.setText(editing.getName());
            inputName.dismissDropDown();
            inputQuantity.setText(UiUtils.formatQuantity(editing.getQuantity()));
            inputUnit.setText(editing.getUnit());
            inputUnit.dismissDropDown();
            inputExpiry.setText(editing.getExpiryDate() == null ? "" : editing.getExpiryDate());
        }

        inputExpiry.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.buttonClearDate).setOnClickListener(v -> inputExpiry.setText(""));
        findViewById(R.id.buttonSave).setOnClickListener(v -> save());
    }

    /**
     * Name field suggests the known ingredients (so the user picks "Tomatoes" instead
     * of inventing a new spelling) and fills in that ingredient's usual unit.
     */
    private void setUpSuggestions() {
        final Map<String, String> unitByName = new HashMap<>();
        List<String> names = new ArrayList<>();
        for (Ingredient known : db.getAllIngredients()) {
            names.add(known.getName());
            unitByName.put(known.getName(), known.getUnit());
        }
        inputName.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, names));
        inputName.setOnItemClickListener((parent, view, position, id) -> {
            String picked = String.valueOf(parent.getItemAtPosition(position));
            String unit = unitByName.get(picked);
            if (unit != null && editing == null) {
                inputUnit.setText(unit);
                inputUnit.dismissDropDown();
            }
            inputQuantity.requestFocus();
        });

        inputUnit.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, COMMON_UNITS));
        inputUnit.setOnClickListener(v -> inputUnit.showDropDown());
    }

    private void showDatePicker() {
        LocalDate start = Ingredient.parseDate(inputExpiry.getText().toString());
        if (start == null) {
            start = LocalDate.now();
        }
        // DatePickerDialog months are 0-based; LocalDate months are 1-based.
        new DatePickerDialog(this, (view, year, month, day) ->
                inputExpiry.setText(String.format(Locale.US, "%04d-%02d-%02d",
                        year, month + 1, day)),
                start.getYear(), start.getMonthValue() - 1, start.getDayOfMonth()).show();
    }

    /** Validate, then CREATE or UPDATE. Closes the screen only on success. */
    private void save() {
        clearErrors();

        String name = inputName.getText().toString().trim();
        String unit = inputUnit.getText().toString().trim();
        String quantityText = inputQuantity.getText().toString().trim();
        String expiry = inputExpiry.getText().toString().trim();

        // ---- Input validation ----
        boolean valid = true;
        if (name.isEmpty()) {
            layoutName.setError("Please enter an ingredient name");
            valid = false;
        } else if (IngredientNames.key(name).isEmpty()) {
            layoutName.setError("The name must contain letters or numbers");
            valid = false;
        }

        double quantity = 0;
        if (quantityText.isEmpty()) {
            layoutQuantity.setError("Please enter a quantity");
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(quantityText);
                if (Double.isNaN(quantity) || Double.isInfinite(quantity) || quantity < 0) {
                    layoutQuantity.setError("Quantity cannot be negative");
                    valid = false;
                } else if (editing == null && quantity == 0) {
                    layoutQuantity.setError("Quantity must be more than 0");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                layoutQuantity.setError("Enter a valid number, e.g. 250 or 0.5");
                valid = false;
            }
        }

        if (unit.isEmpty()) {
            layoutUnit.setError("Please enter a unit (g, ml, piece...)");
            valid = false;
        }
        if (!expiry.isEmpty() && Ingredient.parseDate(expiry) == null) {
            layoutExpiry.setError("Use the date picker (yyyy-MM-dd)");
            valid = false;
        }
        if (!valid) {
            return;
        }

        String expiryOrNull = expiry.isEmpty() ? null : expiry;
        if (editing == null) {
            createIngredient(name, quantity, unit, expiryOrNull);
        } else {
            updateIngredient(name, quantity, unit, expiryOrNull);
        }
    }

    private void createIngredient(String name, double quantity, String unit, String expiry) {
        long result = db.addIngredient(name, quantity, unit, expiry);

        if (result == DatabaseHelper.ERR_UNIT_MISMATCH) {
            layoutUnit.setError("This ingredient is already stored in a different kind of unit");
            return;
        }
        if (result < 0) {
            Toast.makeText(this, "Could not save the ingredient", Toast.LENGTH_LONG).show();
            return;
        }

        // If the user typed "tomato" and the database already has "Tomatoes",
        // the stock was added to that row; tell them so it is not a surprise.
        Ingredient saved = db.getIngredient(result);
        if (saved != null && !saved.getName().equalsIgnoreCase(name)) {
            Toast.makeText(this, "Added to existing ingredient \"" + saved.getName() + "\"",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void updateIngredient(String name, double quantity, String unit, String expiry) {
        boolean ok = db.updateIngredient(
                new Ingredient(editing.getId(), name, quantity, unit, expiry));
        if (!ok) {
            layoutName.setError("Another ingredient already has this name");
            return;
        }
        Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void clearErrors() {
        layoutName.setError(null);
        layoutQuantity.setError(null);
        layoutUnit.setError(null);
        layoutExpiry.setError(null);
    }
}
