package ca.gbc.comp3074.comp3074;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import ca.gbc.comp3074.comp3074.data.AppDatabase;
import ca.gbc.comp3074.comp3074.data.User;
import ca.gbc.comp3074.comp3074.data.UserDao;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private SessionManager sessionManager;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize SessionManager and UserDao
        sessionManager = new SessionManager(this);
        userDao = AppDatabase.getInstance(this).userDao();

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
        TextView tvCreateAccount = findViewById(R.id.tvCreateAccount);

        // Login button logic
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get fresh instance of userDao
            UserDao freshUserDao = AppDatabase.getInstance(this).userDao();

            // Try to get user directly
            User user = freshUserDao.getUserByUsername(username);

            if (user != null) {
                // User exists, check password
                if (user.getPassword().equals(password)) {
                    sessionManager.login(username);
                    sessionManager.setRememberMe(false);
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
        tvCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
