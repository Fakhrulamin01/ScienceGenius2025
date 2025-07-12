package com.example.sciencegenius2025;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MicroorganismActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private TextView titleView, section1View, section2View, section3View, section4View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microorganism_fun);

        Button backButton = findViewById(R.id.backButton2);
        Button nextButton = findViewById(R.id.nextButton2);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(MicroorganismActivity.this, ChaptersActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        nextButton.setOnClickListener(v -> {
            Intent intent = new Intent(MicroorganismActivity.this, SolarSystemActivity.class);
            startActivity(intent);
        });


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Link TextViews from layout
        titleView = findViewById(R.id.title);
        section1View = findViewById(R.id.section1);
        section2View = findViewById(R.id.section2);
        section3View = findViewById(R.id.section3);
        section4View = findViewById(R.id.section4);


        // Fetch content from Firestore
        db.collection("chapters").document("microorganism")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        // Set text if fields are found
                        titleView.setText(snapshot.getString("title"));
                        section1View.setText(snapshot.getString("section1"));
                        section2View.setText(snapshot.getString("section2"));
                        section3View.setText(snapshot.getString("section3"));
                        section4View.setText(snapshot.getString("section4"));
                    } else {
                        Toast.makeText(this, "Chapter data not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load content: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

    }
}
