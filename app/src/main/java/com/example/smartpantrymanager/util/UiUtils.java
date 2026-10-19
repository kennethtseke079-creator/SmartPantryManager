package com.example.smartpantrymanager.util;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Small helpers shared by all screens. */
public final class UiUtils {

    private static final DecimalFormat QTY_FORMAT =
            new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.US));

    private UiUtils() { }

    /**
     * The app draws edge-to-edge, so the root layout is padded by the height of
     * the status and navigation bars. Call once per screen on the root view.
     */
    public static void applySystemBarInsets(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    /** 250.0 becomes "250", 0.5 stays "0.5", 1.333 becomes "1.33". */
    public static String formatQuantity(double quantity) {
        return QTY_FORMAT.format(quantity);
    }
}
