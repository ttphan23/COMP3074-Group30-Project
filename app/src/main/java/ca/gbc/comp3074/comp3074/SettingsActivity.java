package ca.gbc.comp3074.comp3074;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {

    private EditText etUsername, etDisplayName, etEmail, etBio;
    private TextView tvProfilePicture;
    private Button btnBack, btnChangePhoto, btnSaveChanges, btnDeleteAccount;
    private SwitchCompat switchNotifications, switchPrivacy;
    private SessionManager sessionManager;
    private SharedPreferences preferences;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private String selectedProfileEmoji = "👤";

    // Emoji options for profile pictures
    private final String[] emojiOptions = {
        "👤", "🎮", "🎯", "🚀", "⭐", "🔥", "💎", "🎨",
        "🦁", "🐼", "🦊", "🐯", "🦉", "🐺", "🦄", "🐉",
        "😎", "🤓", "🥳", "😇", "🤩", "🥷", "🧙", "🦸"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sessionManager = new SessionManager(this);
        preferences = getSharedPreferences("UserProfile", MODE_PRIVATE);

        initializeViews();
        loadUserData();
        setupListeners();
        setupImagePicker();
    }

    private void initializeViews() {
        etUsername = findViewById(R.id.etUsername);
        etDisplayName = findViewById(R.id.etDisplayName);
        etEmail = findViewById(R.id.etEmail);
        etBio = findViewById(R.id.etBio);
        tvProfilePicture = findViewById(R.id.tvProfilePicture);
        btnBack = findViewById(R.id.btnBack);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchPrivacy = findViewById(R.id.switchPrivacy);
    }

    private void loadUserData() {
        String username = sessionManager.getUsername();
        etUsername.setText(username);
        
        // Load saved profile data
        etDisplayName.setText(preferences.getString("displayName", username));
        etEmail.setText(preferences.getString("email", ""));
        etBio.setText(preferences.getString("bio", ""));
        selectedProfileEmoji = preferences.getString("profileEmoji", "👤");
        tvProfilePicture.setText(selectedProfileEmoji);
        
        // Load preferences
        switchNotifications.setChecked(preferences.getBoolean("notifications", true));
        switchPrivacy.setChecked(preferences.getBoolean("privateProfile", false));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            finish();
        });

        btnChangePhoto.setOnClickListener(v -> {
            showEmojiPicker();
        });

        btnSaveChanges.setOnClickListener(v -> {
            saveChanges();
        });

        btnDeleteAccount.setOnClickListener(v -> {
            showDeleteAccountDialog();
        });
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        // For simplicity, we'll just show a placeholder
                        // In a real app, you'd compress and store the image
                        Toast.makeText(this, "Image selected! (Feature in development)", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }

    private void showEmojiPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Your Avatar");
        
        builder.setItems(emojiOptions, (dialog, which) -> {
            selectedProfileEmoji = emojiOptions[which];
            tvProfilePicture.setText(selectedProfileEmoji);
            
            // Bounce animation
            tvProfilePicture.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(150)
                .withEndAction(() -> {
                    tvProfilePicture.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start();
                })
                .start();
            
            Toast.makeText(this, "Avatar updated! Don't forget to save.", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveChanges() {
        String username = etUsername.getText().toString().trim();
        String displayName = etDisplayName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        // Validation
        if (username.isEmpty()) {
            etUsername.setError("Username required");
            shakeView(etUsername);
            return;
        }

        if (displayName.isEmpty()) {
            displayName = username; // Use username as display name if empty
        }

        // Save to preferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("displayName", displayName);
        editor.putString("email", email);
        editor.putString("bio", bio);
        editor.putString("profileEmoji", selectedProfileEmoji);
        editor.putBoolean("notifications", switchNotifications.isChecked());
        editor.putBoolean("privateProfile", switchPrivacy.isChecked());
        editor.apply();

        // Show success animation
        btnSaveChanges.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction(() -> {
                btnSaveChanges.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start();
            })
            .start();

        Toast.makeText(this, "✅ Settings saved successfully!", Toast.LENGTH_SHORT).show();
        
        // Return to previous screen after a short delay
        btnSaveChanges.postDelayed(() -> {
            finish();
        }, 1000);
    }

    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚠️ Delete Account");
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.");
        
        builder.setPositiveButton("Delete", (dialog, which) -> {
            // Clear all user data
            sessionManager.logout();
            preferences.edit().clear().apply();
            
            Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
            
            // Navigate to login
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void shakeView(View view) {
        view.animate()
            .translationX(-10f)
            .setDuration(50)
            .withEndAction(() -> {
                view.animate()
                    .translationX(10f)
                    .setDuration(50)
                    .withEndAction(() -> {
                        view.animate()
                            .translationX(0f)
                            .setDuration(50)
                            .start();
                    })
                    .start();
            })
            .start();
    }
}
