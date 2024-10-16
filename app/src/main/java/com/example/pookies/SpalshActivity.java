package com.example.pookies;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pookies.R;
public class SpalshActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Find the ImageView
        ImageView loadingImage = findViewById(R.id.Loading_vw);

        // Load the rotate animation
        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.spin);

        // Set an AnimationListener to detect when the animation ends
        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Do nothing when the animation starts
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // When the animation ends, transition to RegisterActivity
                Intent intent = new Intent(SpalshActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Close the splash activity
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Do nothing on animation repeat
            }
        });

        // Start the animation
        loadingImage.startAnimation(rotateAnimation);
    }
}
