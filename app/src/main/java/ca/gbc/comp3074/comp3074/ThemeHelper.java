package ca.gbc.comp3074.comp3074;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeHelper {

    public enum ThemeMode {
        SYSTEM,
        LIGHT,
        DARK
    }

    private ThemeHelper() {
        // Utility class
    }

    public static void applyTheme(@NonNull Context context) {
        setNightMode(getSavedThemeMode(context));
    }

    public static ThemeMode getSavedThemeMode(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SessionManager.PREF_NAME, Context.MODE_PRIVATE);
        int ordinal = prefs.getInt(SessionManager.KEY_THEME_MODE, ThemeMode.SYSTEM.ordinal());
        ThemeMode[] modes = ThemeMode.values();
        if (ordinal < 0 || ordinal >= modes.length) {
            return ThemeMode.SYSTEM;
        }
        return modes[ordinal];
    }

    public static void setThemeMode(@NonNull Context context, @NonNull ThemeMode mode) {
        SharedPreferences prefs = context.getSharedPreferences(SessionManager.PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(SessionManager.KEY_THEME_MODE, mode.ordinal()).apply();
        setNightMode(mode);
    }

    private static void setNightMode(@NonNull ThemeMode mode) {
        int nightMode;
        switch (mode) {
            case LIGHT:
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case DARK:
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }
}
