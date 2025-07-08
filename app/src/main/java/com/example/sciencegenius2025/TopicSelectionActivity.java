package com.example.sciencegenius2025;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class TopicSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_selection);

        // Initialize card views
        CardView cardHumans = findViewById(R.id.cardHumans);
        CardView cardMicroorganism = findViewById(R.id.cardMicroorganism);
        CardView cardSolarSystem = findViewById(R.id.cardSolarSystem);

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
}