package com.example.sciencegenius2025;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.TextView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseUser;

public class MenuActivity extends AppCompatActivity {

    private CardView cardChapters, cardViewModel, cardQuiz, cardLogout;
    private TextView tvUsername;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        tvUsername = findViewById(R.id.tvUsername);
        cardChapters = findViewById(R.id.cardChapters);
        cardViewModel = findViewById(R.id.cardViewModel);
        cardQuiz = findViewById(R.id.cardQuiz);
        cardLogout = findViewById(R.id.cardLogout);

        // Load animations
        Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.card_bounce);

        // Set animations with staggered delays
        cardChapters.startAnimation(bounceAnim);
        cardViewModel.startAnimation(bounceAnim);
        cardQuiz.startAnimation(bounceAnim);
        cardLogout.startAnimation(bounceAnim);

        // Add press effect
        setupCardPressEffects();

        // Load user data
        loadUserData();

        // Set click listeners
        setupClickListeners();
    }

    private void setupCardPressEffects() {
        int[] cards = {R.id.cardChapters, R.id.cardViewModel, R.id.cardQuiz, R.id.cardLogout};

        for (int cardId : cards) {
            CardView card = findViewById(cardId);
            card.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        // Small bounce effect when released
                        Animation smallBounce = AnimationUtils.loadAnimation(this, R.anim.card_bounce);
                        smallBounce.setDuration(300);
                        v.startAnimation(smallBounce);
                        break;
                }
                return false;
            });
        }
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            tvUsername.setText("🎉 Welcome, " + username + "!");
                        } else {
                            tvUsername.setText("Oops! We couldn't find your info.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvUsername.setText("⚠️ Error loading your profile");
                    });
        }
    }

    private void setupClickListeners() {
        cardChapters.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Chapters...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ChaptersActivity.class));
        });

        cardViewModel.setOnClickListener(v -> {
            Toast.makeText(this, "Opening 3D Model Viewer...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ModelActivity.class));
        });

        cardQuiz.setOnClickListener(v -> {
            Toast.makeText(this, "Starting Quiz...", Toast.LENGTH_SHORT).show();
        });

        cardLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Goodbye Genius!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up animations
        if (cardChapters != null) cardChapters.clearAnimation();
        if (cardViewModel != null) cardViewModel.clearAnimation();
        if (cardQuiz != null) cardQuiz.clearAnimation();
        if (cardLogout != null) cardLogout.clearAnimation();
    }
}