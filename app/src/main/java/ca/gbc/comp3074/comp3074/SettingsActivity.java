package ca.gbc.comp3074.comp3074;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchDarkMode;
    private SwitchCompat switchPush;
    private Button btnChangePassword;
    private Button btnPrivacy;
    private Button btnLogout;
    private ImageButton btnBack;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sessionManager = new SessionManager(this);

        // Back button
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchPush = findViewById(R.id.switchPush);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnPrivacy = findViewById(R.id.btnPrivacy);
        btnLogout = findViewById(R.id.btnLogout);

        switchDarkMode.setChecked(ThemeHelper.getSavedThemeMode(this) == ThemeHelper.ThemeMode.DARK);
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeHelper.ThemeMode next = isChecked ? ThemeHelper.ThemeMode.DARK : ThemeHelper.ThemeMode.LIGHT;
            if (ThemeHelper.getSavedThemeMode(this) != next) {
                ThemeHelper.setThemeMode(this, next);
                recreate();
            }
        });

        switchPush.setOnCheckedChangeListener((buttonView, isChecked) -> Toast.makeText(this, isChecked ? "Push notifications enabled" : "Push notifications disabled", Toast.LENGTH_SHORT).show());

        btnChangePassword.setOnClickListener(v -> Toast.makeText(this, "Password change flow TBD", Toast.LENGTH_SHORT).show());
        btnPrivacy.setOnClickListener(v -> Toast.makeText(this, "Privacy options coming soon", Toast.LENGTH_SHORT).show());
        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

}
