package com.example.sciencegenius2025;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class TopicSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_selection);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // Closes the activity

        // Initialize card views
        CardView cardHumans = findViewById(R.id.cardHumans);
        CardView cardMicroorganism = findViewById(R.id.cardMicroorganism);
        CardView cardSolarSystem = findViewById(R.id.cardSolarSystem);

        applyCardTouchEffect(cardHumans);
        applyCardTouchEffect(cardMicroorganism);
        applyCardTouchEffect(cardSolarSystem);

        // Set click listeners
        cardHumans.setOnClickListener(v -> startQuiz("Humans"));
        cardMicroorganism.setOnClickListener(v -> startQuiz("Microorganism"));
        cardSolarSystem.setOnClickListener(v -> startQuiz("Solar System"));
    }

    private void startQuiz(String topic) {
        Intent intent = new Intent(TopicSelectionActivity.this, QuizActivity.class);
        intent.putExtra("topic", topic);
        startActivity(intent);
        // Optional: Add animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void applyCardTouchEffect(CardView card) {
        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    card.setCardElevation(2f); // lower elevation = pressed
                    card.setScaleX(0.98f);     // slightly scale down for 3D feel
                    card.setScaleY(0.98f);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    card.setCardElevation(8f); // restore elevation
                    card.setScaleX(1f);
                    card.setScaleY(1f);
                    break;
            }
            return false; // so normal click still works
        });
    }


}