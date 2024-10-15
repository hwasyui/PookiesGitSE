package com.example.pookies;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvSignUp;
    DBHelper dbHelper;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DBHelper(this);  // Initialize SQLite DB Helper
        mAuth = FirebaseAuth.getInstance();  // Initialize Firebase Auth

        etEmail = findViewById(R.id.emailInput);
        etPassword = findViewById(R.id.passwordInput);
        btnLogin = findViewById(R.id.loginButton);
        tvSignUp = findViewById(R.id.alreadyHaveAccount);

        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                } else {
                    // First, try to authenticate with Firebase
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Firebase authentication successful
                                        loginSuccess(email);
                                    } else {
                                        // Firebase authentication failed, try SQLite
                                        authenticateWithSQLite(email, password);
                                    }
                                }
                            });
                }
            }
        });
    }

    private void authenticateWithSQLite(String email, String password) {
        User user = dbHelper.getUserByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            // SQLite authentication successful
            loginSuccess(email);
        } else {
            // Both Firebase and SQLite authentication failed
            Toast.makeText(LoginActivity.this, "Authentication failed: Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }

    private void loginSuccess(String email) {
        // Save the user ID (email) in SharedPreferences for session management
        getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .edit()
                .putString("USER_ID", email)
                .apply();

        // Login successful, navigate to ChatActivity
        Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        startActivity(intent);
        finish();  // Finish LoginActivity
    }
}