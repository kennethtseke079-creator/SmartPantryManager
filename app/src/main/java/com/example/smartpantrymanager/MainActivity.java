package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.adapter.IngredientAdapter;
import com.example.smartpantrymanager.data.AppSettings;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.model.Ingredient;
import com.example.smartpantrymanager.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * Screen 1: Pantry List (the launcher screen).
 *
 * <p>Shows every ingredient that is currently in stock, read from SQLite and
 * displayed with a custom RecyclerView adapter. From here the user navigates with
 * Intents to the Add/Edit, Suggested Recipes and Settings screens.</p>
 */
public class MainActivity extends AppCompatActivity implements IngredientAdapter.Listener {

    private DatabaseHelper db;
    private AppSettings settings;
    private IngredientAdapter adapter;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        UiUtils.applySystemBarInsets(findViewById(R.id.main));

        db = DatabaseHelper.getInstance(this);   // creates + seeds the DB on first run
        settings = new AppSettings(this);

        // Toolbar menu = the app's navigation element (Recipes, Settings).
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_recipes) {
                openSuggestedRecipes();
                return true;
            } else if (id == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });

        emptyView = findViewById(R.id.emptyView);
        adapter = new IngredientAdapter(this);
        RecyclerView list = findViewById(R.id.pantryList);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        findViewById(R.id.addFab).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));
        findViewById(R.id.cookButton).setOnClickListener(v -> openSuggestedRecipes());
    }

    /**
     * onResume runs every time this screen comes back to the front (after adding,
     * editing, or changing settings), so the list is always up to date.
     */
    @Override
    protected void onResume() {
        super.onResume();
        refreshPantry();
    }

    private void refreshPantry() {
        List<Ingredient> items = db.getPantryItems();
        adapter.setItems(items, settings.isHighlightExpiringEnabled(),
                settings.getExpiringSoonDays());
        emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openSuggestedRecipes() {
        startActivity(new Intent(this, SuggestedRecipesActivity.class));
    }

    // ---- IngredientAdapter.Listener ----

    @Override
    public void onEdit(Ingredient ingredient) {
        // Intent extra tells the Add/Edit screen WHICH ingredient to load.
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(AddEditIngredientActivity.EXTRA_INGREDIENT_ID, ingredient.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(final Ingredient ingredient) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Remove " + ingredient.getName() + "?")
                .setMessage("This removes it from your pantry.")
                .setPositiveButton("Remove", (dialog, which) -> {
                    db.deleteIngredient(ingredient.getId());
                    refreshPantry();
                    Toast.makeText(this, ingredient.getName() + " removed",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
