package com.example.pookies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChatActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ImageButton buttonDrawerToogle;
    NavigationView navigationView;
    ImageView userImage;
    TextView textUsername, textEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firebase User
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Get user ID, display name, and email from Firebase
            String userID = currentUser.getUid(); // Store this for session management
            String displayName = currentUser.getDisplayName(); // Display this to the user
            String email = currentUser.getEmail(); // Get user email

            // Save user ID in SharedPreferences
            SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("USER_ID", userID);  // Store user ID in SharedPreferences
            editor.apply();

            // Toast a welcome message with the username
            if (displayName != null && !displayName.isEmpty()) {
                Toast.makeText(this, "Welcome, " + displayName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Welcome, User", Toast.LENGTH_SHORT).show();
            }

            // Initialize navigation drawer and header
            drawerLayout = findViewById(R.id.drawerLayout);
            buttonDrawerToogle = findViewById(R.id.buttonDrawerToggle);
            navigationView = findViewById(R.id.navigationView);

            buttonDrawerToogle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawerLayout.open();
                }
            });

            // Initialize header view for user image and username
            View headerView = navigationView.getHeaderView(0);
            userImage = headerView.findViewById(R.id.userImage);
            textUsername = headerView.findViewById(R.id.textUsername);
            textEmail = headerView.findViewById(R.id.textEmail);  // Assuming you added a TextView for email

            // Set username and email in the header
            if (displayName != null && !displayName.isEmpty()) {
                textUsername.setText(displayName);  // Set username in TextView
            } else {
                textUsername.setText("User");
            }

            textEmail.setText(email);  // Set email in TextView

            // Set click listener for userImage and textUsername to open ProfileFragment
            userImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Load ProfileFragment
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment())
                            .commit();
                    // Close drawer after fragment is replaced
                    drawerLayout.close();
                }
            });

            textUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Load ProfileFragment
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment())
                            .commit();
                    // Close drawer after fragment is replaced
                    drawerLayout.close();
                }
            });

        } else {
            // No user is logged in, redirect to LoginActivity
            Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        // Set navigation item listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                // Replace fragments based on which menu item is clicked
                int itemId = item.getItemId();
                if (itemId == R.id.navChat) {
                    selectedFragment = new ChatFragment();
                } else if (itemId == R.id.navSettings) {
                    selectedFragment = new SettingsFragment();
                } else if (itemId == R.id.navFeedback) {
                    selectedFragment = new FeedbackFragment();
                } else if (itemId == R.id.navAbout) {
                    selectedFragment = new AboutFragment();
                } else if (itemId == R.id.navPolicy) {
                    selectedFragment = new PolicyFragment();
                } else if (itemId == R.id.navLogout) {
                    logOutUser();
                    return true;
                }

                // Replace the fragment if one was selected
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment) // 'fragment_container' is the ID of the FrameLayout in activity_content.xml
                            .commit();
                }

                drawerLayout.close();
                return true;
            }
        });

        // Load default fragment (ChatFragment)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChatFragment())
                    .commit();
        }
    }

    // Method to log out the user
    private void logOutUser() {
        FirebaseAuth.getInstance().signOut(); // Sign out the user

        // Clear UID from SharedPreferences
        getSharedPreferences("APP_PREFS", MODE_PRIVATE).edit().remove("USER_ID").apply();

        // Show a toast message to confirm logout
        Toast.makeText(ChatActivity.this, "You have been logged out", Toast.LENGTH_SHORT).show();

        // Redirect to LoginActivity
        Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish(); // Close ChatActivity
    }
}