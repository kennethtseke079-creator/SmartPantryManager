package com.example.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.Ingredient;
import com.example.smartpantrymanager.util.UiUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerView adapter for the pantry list.
 *
 * <p>Each row shows name, quantity + unit and an expiry line. The screen that owns
 * the adapter is told about taps through {@link Listener}, so the adapter itself
 * never touches the database or starts activities.</p>
 */
public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {

    /** Callbacks implemented by MainActivity. */
    public interface Listener {
        void onEdit(Ingredient ingredient);
        void onDelete(Ingredient ingredient);
    }

    private final List<Ingredient> items = new ArrayList<>();
    private final Listener listener;
    private boolean highlightExpiring = true;
    private int expiringSoonDays = 3;

    public IngredientAdapter(Listener listener) {
        this.listener = listener;
    }

    /** Replaces the list contents and the expiry-highlight settings, then redraws. */
    public void setItems(List<Ingredient> newItems, boolean highlightExpiring,
                         int expiringSoonDays) {
        items.clear();
        items.addAll(newItems);
        this.highlightExpiring = highlightExpiring;
        this.expiringSoonDays = expiringSoonDays;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Ingredient item = items.get(position);

        holder.name.setText(item.getName());
        holder.quantity.setText(UiUtils.formatQuantity(item.getQuantity())
                + " " + item.getUnit());
        bindExpiry(holder, item);

        holder.itemView.setOnClickListener(v -> listener.onEdit(item));
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(item));
    }

    private void bindExpiry(ViewHolder holder, Ingredient item) {
        LocalDate expiry = Ingredient.parseDate(item.getExpiryDate());
        if (expiry == null) {
            holder.expiry.setVisibility(View.GONE);
            return;
        }
        holder.expiry.setVisibility(View.VISIBLE);

        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), expiry);
        View view = holder.expiry;
        int normal = holder.quantity.getCurrentTextColor();

        if (daysLeft < 0) {
            holder.expiry.setText("EXPIRED on " + item.getExpiryDate());
            holder.expiry.setTextColor(view.getContext().getColor(R.color.expired_red));
        } else if (highlightExpiring && daysLeft <= expiringSoonDays) {
            String when = daysLeft == 0 ? "today"
                    : daysLeft == 1 ? "tomorrow" : "in " + daysLeft + " days";
            holder.expiry.setText("Expires " + when + " (" + item.getExpiryDate() + ")");
            holder.expiry.setTextColor(view.getContext().getColor(R.color.expiring_amber));
        } else {
            holder.expiry.setText("Expires " + item.getExpiryDate());
            holder.expiry.setTextColor(normal);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Holds the row's views so findViewById runs once per row, not per bind. */
    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView quantity;
        final TextView expiry;
        final Button deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textName);
            quantity = itemView.findViewById(R.id.textQuantity);
            expiry = itemView.findViewById(R.id.textExpiry);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
