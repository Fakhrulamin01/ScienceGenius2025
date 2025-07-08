package com.example.sciencegenius2025;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private TextView tvResultMessage, tvScore;
    private Button btnBackToTopics;
    private LinearLayout resultLayout;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvResultMessage = findViewById(R.id.tvResultMessage);
        tvScore = findViewById(R.id.tvScore);
        btnBackToTopics = findViewById(R.id.btnBackToTopics);
        resultLayout = findViewById(R.id.resultLayout);

        int score = getIntent().getIntExtra("score", 0);
        String topic = getIntent().getStringExtra("topic");

        tvScore.setText("Your Score: " + score + "/10");

        // Feedback based on score
        if (score >= 8) {
            tvResultMessage.setText("🎉 Great Job!");
            resultLayout.setBackgroundColor(getResources().getColor(R.color.result_high));
            mediaPlayer = MediaPlayer.create(this, R.raw.success_sound);
        } else if (score >= 5) {
            tvResultMessage.setText("👍 Good Effort!");
            resultLayout.setBackgroundColor(getResources().getColor(R.color.result_medium));
            mediaPlayer = MediaPlayer.create(this, R.raw.moderate_sound);
        } else {
            tvResultMessage.setText("💪 Keep Practicing!");
            resultLayout.setBackgroundColor(getResources().getColor(R.color.result_low));
            mediaPlayer = MediaPlayer.create(this, R.raw.encouragement_sound);
        }

        // Play sound
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }

        // Animate result elements
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_scale);
        tvResultMessage.startAnimation(fadeIn);
        tvScore.startAnimation(fadeIn);
        btnBackToTopics.startAnimation(fadeIn);

        // Button to go back to topics
        btnBackToTopics.setOnClickListener(v -> {
            if (mediaPlayer != null) mediaPlayer.release();

            Intent intent = new Intent(ResultActivity.this, TopicSelectionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Optional Toast to confirm screen loaded
        Toast.makeText(this, "Quiz Result Shown", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
