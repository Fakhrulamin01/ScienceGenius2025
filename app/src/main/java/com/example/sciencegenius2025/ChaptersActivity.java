package com.example.sciencegenius2025;


import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ChaptersActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapters); // Your XML layout

        // Human Body Systems Card
        CardView cardHumanBody = findViewById(R.id.cardHumanBody);
        cardHumanBody.setOnClickListener(v -> {
            startActivity(new Intent(this, HumanBodyActivity.class));
        });

        // Microorganisms Card
        CardView cardMicroorganisms = findViewById(R.id.cardMicroorganisms);
        cardMicroorganisms.setOnClickListener(v -> {
            startActivity(new Intent(this, MicroorganismActivity.class));
        });

        // Solar System Card
        CardView cardSolarSystem = findViewById(R.id.cardSolarSystem);
        cardSolarSystem.setOnClickListener(v -> {
            startActivity(new Intent(this, SolarSystemActivity.class));
        });
    }
}
