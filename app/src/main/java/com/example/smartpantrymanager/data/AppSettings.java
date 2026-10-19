package com.example.smartpantrymanager.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * User preferences stored in SharedPreferences (a small key/value file that
 * survives app restarts). Read by the pantry list (expiry highlighting) and the
 * suggested-recipes screen (whether expired stock counts).
 */
public class AppSettings {

    private static final String PREFS_NAME = "smart_pantry_settings";
    private static final String KEY_HIGHLIGHT_EXPIRING = "highlight_expiring";
    private static final String KEY_EXPIRING_DAYS = "expiring_days";
    private static final String KEY_IGNORE_EXPIRED = "ignore_expired";

    private final SharedPreferences prefs;

    public AppSettings(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Highlight items that are about to expire in the pantry list (default on). */
    public boolean isHighlightExpiringEnabled() {
        return prefs.getBoolean(KEY_HIGHLIGHT_EXPIRING, true);
    }

    public void setHighlightExpiringEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_HIGHLIGHT_EXPIRING, enabled).apply();
    }

    /** "Expiring soon" means within this many days (default 3). */
    public int getExpiringSoonDays() {
        return prefs.getInt(KEY_EXPIRING_DAYS, 3);
    }

    public void setExpiringSoonDays(int days) {
        prefs.edit().putInt(KEY_EXPIRING_DAYS, days).apply();
    }

    /** Expired stock does not count towards recipe suggestions (default on). */
    public boolean isIgnoreExpiredInSuggestions() {
        return prefs.getBoolean(KEY_IGNORE_EXPIRED, true);
    }

    public void setIgnoreExpiredInSuggestions(boolean ignore) {
        prefs.edit().putBoolean(KEY_IGNORE_EXPIRED, ignore).apply();
    }
}