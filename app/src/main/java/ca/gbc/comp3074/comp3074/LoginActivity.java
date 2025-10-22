package ca.gbc.comp3074.comp3074;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Skip login if already logged in
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            finish();
            return;
        }

        // View bindings
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvGuestLogin = findViewById(R.id.tvGuestLogin);

        // Login button logic
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if user exists
            if (sessionManager.userExists(username)) {
                // Validate password (for demo, just check it's not empty)
                if (sessionManager.validateUserPassword(username, password)) {
                    sessionManager.login(username);
                    Toast.makeText(this, "Welcome back, " + username + "!", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                    finish();
                } else {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "User not found. Please register first.", Toast.LENGTH_SHORT).show();
            }
        });

        // Register button logic
        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate username length
            if (username.length() < 3) {
                Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate password length
            if (password.length() < 4) {
                Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if username already exists
            if (sessionManager.userExists(username)) {
                Toast.makeText(this, "Username already exists. Please login or choose a different username.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Register new user
            sessionManager.registerUser(username, password);
            sessionManager.login(username);
            Toast.makeText(this, "Registration successful! Welcome, " + username + "!", Toast.LENGTH_SHORT).show();
            navigateToMain();
            finish();
        });

        // Guest login button logic
        tvGuestLogin.setOnClickListener(v -> {
            sessionManager.loginAsGuest();
            Toast.makeText(this, "Continuing as Guest", Toast.LENGTH_SHORT).show();
            navigateToMain();
            finish();
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
