package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.adapter.RecipeAdapter;
import com.example.smartpantrymanager.data.AppSettings;
import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.logic.RecipeMatcher;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

import java.time.LocalDate;
import java.util.List;

/**
 * Screen 3: Suggested Recipes.
 *
 * <p>Runs the STRICT matching rule: a recipe is listed only if every one of its
 * ingredients is in the pantry in at least the required quantity. When nothing
 * qualifies, a clear message is shown instead of a blank list.</p>
 */
public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.Listener {

    private DatabaseHelper db;
    private AppSettings settings;
    private RecipeAdapter adapter;
    private TextView summary;
    private View emptyView;
    private RecyclerView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suggested_recipes);
        UiUtils.applySystemBarInsets(findViewById(R.id.main));

        db = DatabaseHelper.getInstance(this);
        settings = new AppSettings(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        summary = findViewById(R.id.textSummary);
        emptyView = findViewById(R.id.emptyView);
        list = findViewById(R.id.recipeList);
        adapter = new RecipeAdapter(this);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        findViewById(R.id.buttonAddIngredients).setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));
    }

    /** Re-runs the matching every time the screen is shown (pantry may have changed). */
    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        RecipeMatcher matcher = new RecipeMatcher(
                settings.isIgnoreExpiredInSuggestions(), LocalDate.now());

        List<Recipe> cookable = matcher.findCookableRecipes(
                db.getPantryItems(), db.getAllRecipes());

        adapter.setItems(cookable);

        boolean none = cookable.isEmpty();
        emptyView.setVisibility(none ? View.VISIBLE : View.GONE);
        list.setVisibility(none ? View.GONE : View.VISIBLE);
        summary.setText(none ? "" : cookable.size() + (cookable.size() == 1
                ? " recipe you can cook right now"
                : " recipes you can cook right now"));
    }

    @Override
    public void onRecipeClicked(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
