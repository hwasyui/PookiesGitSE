package com.example.pookies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

public class ChatActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "APP_PREFS";
    private static final String USER_ID_KEY = "USER_ID";

    DrawerLayout drawerLayout;
    ImageButton buttonDrawerToggle;
    NavigationView navigationView;
    ImageView userImage;
    TextView textUsername, textEmail;
    SharedPreferences prefs;
    FirebaseAuth mAuth;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        dbHelper = new DBHelper(this);

        if (!isUserLoggedIn()) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_chat);

        initializeViews();
        setupNavigationDrawer();
        loadUserData();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChatFragment())
                    .commit();
        }
    }

    private boolean isUserLoggedIn() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String userID = prefs.getString(USER_ID_KEY, null);
        return firebaseUser != null || userID != null;
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        buttonDrawerToggle = findViewById(R.id.buttonDrawerToggle);
        navigationView = findViewById(R.id.navigationView);

        View headerView = navigationView.getHeaderView(0);
        userImage = headerView.findViewById(R.id.userImage);
        textUsername = headerView.findViewById(R.id.textUsername);
        textEmail = headerView.findViewById(R.id.textEmail);
    }

    private void setupNavigationDrawer() {
        buttonDrawerToggle.setOnClickListener(v -> drawerLayout.open());

        userImage.setOnClickListener(v -> loadProfileFragment());
        textUsername.setOnClickListener(v -> loadProfileFragment());

        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.navChat) {
                selectedFragment = new ChatFragment();
            } else if (itemId == R.id.navSettings) {
                selectedFragment = new SettingsFragment();
            } else if (itemId == R.id.navFeedback) {
                selectedFragment = new FeedbackFragment();
            } else if (itemId == R.id.navLogout) {
                logOutUser();
                return true;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            drawerLayout.close();
            return true;
        });
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        String userID = prefs.getString(USER_ID_KEY, null);

        if (firebaseUser != null) {
            // User is logged in with Firebase
            textUsername.setText(firebaseUser.getDisplayName());
            textEmail.setText(firebaseUser.getEmail());
            Toast.makeText(this, "Welcome, " + firebaseUser.getDisplayName(), Toast.LENGTH_SHORT).show();
        } else if (userID != null) {
            // User is logged in with SharedPreferences
            User currentUser = dbHelper.getUserByEmail(userID);
            if (currentUser != null) {
                textUsername.setText(currentUser.getName());
                textEmail.setText(currentUser.getEmail());
                Toast.makeText(this, "Welcome, " + currentUser.getName(), Toast.LENGTH_SHORT).show();
            } else {
                // User ID in SharedPreferences is invalid
                logOutUser();
                return;
            }
        }

        loadProfilePicture();
    }

    private void loadProfilePicture() {
        String profilePicUri = prefs.getString("PROFILE_PIC_URI", null);
        if (profilePicUri != null) {
            try {
                Uri uri = Uri.parse(profilePicUri);
                userImage.setImageURI(uri);

                // If setImageURI fails (returns null drawable), fall back to bitmap method
                if (userImage.getDrawable() == null) {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    userImage.setImageBitmap(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load profile picture", Toast.LENGTH_SHORT).show();
                userImage.setImageResource(R.drawable.person);
            }
        } else {
            userImage.setImageResource(R.drawable.person);
        }
    }

    private void loadProfileFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ProfileFragment())
                .commit();
        drawerLayout.close();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logOutUser() {
        mAuth.signOut(); // Sign out from Firebase
        prefs.edit().remove(USER_ID_KEY).apply(); // Clear SharedPreferences

        Toast.makeText(ChatActivity.this, "You have been logged out", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isUserLoggedIn()) {
            redirectToLogin();
        } else {
            loadProfilePicture();
        }
    }
}