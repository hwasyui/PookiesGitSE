package com.example.pookies;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.*;

import android.content.Intent;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;


import com.google.android.material.navigation.NavigationView;

public class ChatActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ImageButton buttonDrawerToogle;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userID = currentUser.getUid(); // Store this for session management
            String displayName = currentUser.getDisplayName(); // Display this to the user

            SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("USER_ID", userID);  // Store user ID in SharedPreferences
            editor.apply();

            // Display the username in the UI (or fallback to "User" if display name is not set)
            if (displayName != null && !displayName.isEmpty()) {
                Toast.makeText(this, "Welcome, " + displayName, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Welcome, User", Toast.LENGTH_SHORT).show();
            }
        } else {
            // No user is logged in, redirect to LoginActivity
            Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }


        drawerLayout = findViewById(R.id.drawerLayout);
        buttonDrawerToogle = findViewById(R.id.buttonDrawerToggle);
        navigationView = findViewById(R.id.navigationView);

        buttonDrawerToogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.open();
            }
        });

        View headerView = navigationView.getHeaderView(0);
        ImageView userImage = headerView.findViewById(R.id.userImage);
        TextView textUsername = headerView.findViewById(R.id.textUsername);

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

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item){

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
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChatFragment())
                    .commit();
        }
    }
    private void logOutUser() {
        // Sign out the user from Firebase
        FirebaseAuth.getInstance().signOut();

        // Clear UID from SharedPreferences (clear session)
        getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .edit()
                .remove("USER_ID")
                .apply();

        // Show a toast message to confirm logout
        Toast.makeText(ChatActivity.this, "You have been logged out", Toast.LENGTH_SHORT).show();

        // Redirect to LoginActivity
        Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish(); // Close ChatActivity
    }

}