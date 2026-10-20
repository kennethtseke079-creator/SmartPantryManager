package com.example.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/** Custom RecyclerView adapter for the Suggested Recipes list. */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    /** Implemented by SuggestedRecipesActivity to open the detail screen. */
    public interface Listener {
        void onRecipeClicked(Recipe recipe);
    }

    private final List<Recipe> items = new ArrayList<>();
    private final Listener listener;

    public RecipeAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Recipe> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Recipe recipe = items.get(position);
        int count = recipe.getIngredients().size();

        holder.name.setText(recipe.getName());
        holder.info.setText(recipe.getPrepTime() + " min  -  "
                + count + (count == 1 ? " ingredient" : " ingredients"));
        holder.itemView.setOnClickListener(v -> listener.onRecipeClicked(recipe));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView info;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textRecipeName);
            info = itemView.findViewById(R.id.textRecipeInfo);
        }
    }
}
