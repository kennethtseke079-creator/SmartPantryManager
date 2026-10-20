package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.data.AppSettings;
import com.example.smartpantrymanager.util.UiUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * Screen 5: Settings.
 * Each change is saved immediately to SharedPreferences through {@link AppSettings}.
 */
public class SettingsActivity extends AppCompatActivity {

    private AppSettings settings;
    private RadioGroup radioDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        UiUtils.applySystemBarInsets(findViewById(R.id.main));

        settings = new AppSettings(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        MaterialSwitch switchHighlight = findViewById(R.id.switchHighlight);
        MaterialSwitch switchIgnoreExpired = findViewById(R.id.switchIgnoreExpired);
        radioDays = findViewById(R.id.radioDays);

        // 1. Show the saved values BEFORE attaching listeners, so setting the
        //    initial state does not trigger a save.
        switchHighlight.setChecked(settings.isHighlightExpiringEnabled());
        switchIgnoreExpired.setChecked(settings.isIgnoreExpiredInSuggestions());
        selectDaysRadio(settings.getExpiringSoonDays());
        setDaysEnabled(settings.isHighlightExpiringEnabled());

        // 2. Save on every change.
        switchHighlight.setOnCheckedChangeListener((button, checked) -> {
            settings.setHighlightExpiringEnabled(checked);
            setDaysEnabled(checked);
        });
        switchIgnoreExpired.setOnCheckedChangeListener((button, checked) ->
                settings.setIgnoreExpiredInSuggestions(checked));
        radioDays.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio2) {
                settings.setExpiringSoonDays(2);
            } else if (checkedId == R.id.radio7) {
                settings.setExpiringSoonDays(7);
            } else {
                settings.setExpiringSoonDays(3);
            }
        });
    }

    private void selectDaysRadio(int days) {
        int id = days == 2 ? R.id.radio2 : days == 7 ? R.id.radio7 : R.id.radio3;
        radioDays.check(id);
    }

    /** The day choices only make sense while highlighting is switched on. */
    private void setDaysEnabled(boolean enabled) {
        for (int i = 0; i < radioDays.getChildCount(); i++) {
            ((RadioButton) radioDays.getChildAt(i)).setEnabled(enabled);
        }
    }
}
