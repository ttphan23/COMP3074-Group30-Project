package ca.gbc.comp3074.comp3074;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import ca.gbc.comp3074.comp3074.data.AppDatabase;
import ca.gbc.comp3074.comp3074.data.User;
import ca.gbc.comp3074.comp3074.data.UserDao;

public class RegisterActivity extends AppCompatActivity {

    private EditText etEmail, etUsername, etPassword;
    private SessionManager sessionManager;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sessionManager = new SessionManager(this);
        userDao = AppDatabase.getInstance(this).userDao();

        etEmail = findViewById(R.id.etRegisterEmail);
        etUsername = findViewById(R.id.etRegisterUsername);
        etPassword = findViewById(R.id.etRegisterPassword);

        Button btnSubmit = findViewById(R.id.btnSubmitRegister);
        TextView tvBack = findViewById(R.id.tvBackToLogin);

        btnSubmit.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (username.length() < 3) {
                Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 4) {
                Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if user exists in Room DB
            if (userDao.userExists(username) > 0) {
                Toast.makeText(this, "Username already exists. Choose another.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Register user in Room DB
            User newUser = new User(username, password, "", email);
            userDao.insert(newUser);

            sessionManager.setRememberMe(false);
            sessionManager.login(username);

            Toast.makeText(this, "Registration successful! Welcome, " + username + "!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        });

        tvBack.setOnClickListener(v -> finish());
    }
}
