package com.example.pookies;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

        // Click listener for textUsername (username text)
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
                    // Handle logout logic here
                    return false;
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
}