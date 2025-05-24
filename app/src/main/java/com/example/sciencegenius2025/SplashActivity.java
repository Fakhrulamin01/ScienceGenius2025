package com.example.sciencegenius2025;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔥 No need for setContentView() when splash is handled via theme drawable

        mAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(() -> {
            if (mAuth.getCurrentUser() != null) {
                // ✅ Already logged in → go to MenuActivity
                startActivity(new Intent(SplashActivity.this, MenuActivity.class));
            } else {
                // ❌ Not logged in → go to LoginActivity
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish(); // Prevent returning to splash
        }, SPLASH_DELAY);
    }
}
