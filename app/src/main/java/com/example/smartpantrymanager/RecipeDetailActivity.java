package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.data.DatabaseHelper;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;
import com.example.smartpantrymanager.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Screen 4: Recipe Detail.
 * Receives a recipe id through the Intent and shows the full ingredient list
 * and the preparation steps.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    /** Intent extra: id of the recipe to display. */
    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);
        UiUtils.applySystemBarInsets(findViewById(R.id.main));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = DatabaseHelper.getInstance(this).getRecipe(recipeId);
        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.textRecipeTitle)).setText(recipe.getName());
        ((TextView) findViewById(R.id.textPrepTime))
                .setText("Preparation time: " + recipe.getPrepTime() + " minutes");

        StringBuilder ingredients = new StringBuilder();
        for (RecipeIngredient line : recipe.getIngredients()) {
            if (ingredients.length() > 0) {
                ingredients.append('\n');
            }
            ingredients.append("• ")
                    .append(UiUtils.formatQuantity(line.getRequiredQuantity()))
                    .append(' ').append(line.getUnit())
                    .append(' ').append(line.getIngredientName());
        }
        ((TextView) findViewById(R.id.textIngredients)).setText(ingredients.toString());
        ((TextView) findViewById(R.id.textInstructions)).setText(recipe.getInstructions());
    }
}
