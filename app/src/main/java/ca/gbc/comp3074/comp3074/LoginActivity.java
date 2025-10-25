package ca.gbc.comp3074.comp3074;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private CheckBox cbRemember;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Skip login if already logged in AND user chose "Remember me"
        if (sessionManager.isLoggedIn() && sessionManager.isRememberMe()) {
            navigateToMain();
            finish();
            return;
        }

        // View bindings
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        cbRemember = findViewById(R.id.cbRemember);
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
                    // Persist remember preference
                    sessionManager.setRememberMe(cbRemember != null && cbRemember.isChecked());
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

        // Open Register screen
        btnRegister.setOnClickListener(v -> {
            startActivity(new android.content.Intent(LoginActivity.this, RegisterActivity.class));
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
