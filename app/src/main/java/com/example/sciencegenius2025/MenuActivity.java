package com.example.sciencegenius2025;

import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Random;

public class MenuActivity extends AppCompatActivity {

    private CardView cardChapters, cardViewModel, cardQuiz, cardLogout, cardHelp, cardHistory;
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
        cardHelp = findViewById(R.id.cardHelp);
        cardLogout = findViewById(R.id.cardLogout);
        cardHistory = findViewById(R.id.cardHistory);

        // Load animations
        Animation bounceAnim = AnimationUtils.loadAnimation(this, R.anim.card_bounce);
        cardChapters.startAnimation(bounceAnim);
        cardViewModel.startAnimation(bounceAnim);
        cardQuiz.startAnimation(bounceAnim);
        cardHelp.startAnimation(bounceAnim);
        cardLogout.startAnimation(bounceAnim);
        cardHistory.startAnimation(bounceAnim);

        // Press effects
        setupCardPressEffects();

        // Load user data
        loadUserData();

        // Click listeners
        setupClickListeners();
    }

    private void setupCardPressEffects() {
        int[] cards = {R.id.cardChapters, R.id.cardViewModel, R.id.cardQuiz, R.id.cardHistory, R.id.cardHelp, R.id.cardLogout};

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

                            String welcomeText = " Welcome, " + username + "!";
                            tvUsername.setText(welcomeText);

                            Animation bounceIn = AnimationUtils.loadAnimation(this, R.anim.bounce_in);
                            tvUsername.startAnimation(bounceIn);

                            tvUsername.startAnimation(bounceIn);
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
            startActivity(new Intent(this, TopicSelectionActivity.class));
        });

        cardHistory.setOnClickListener(v -> {
            Toast.makeText(this, "Viewing Quiz History...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, QuizHistoryActivity.class));
        });

        cardHelp.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Help...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HelpActivity.class));
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
        if (cardChapters != null) cardChapters.clearAnimation();
        if (cardViewModel != null) cardViewModel.clearAnimation();
        if (cardQuiz != null) cardQuiz.clearAnimation();
        if (cardHelp != null) cardHelp.clearAnimation();
        if (cardLogout != null) cardLogout.clearAnimation();
        if (cardHistory != null) cardHistory.clearAnimation();
    }
}
