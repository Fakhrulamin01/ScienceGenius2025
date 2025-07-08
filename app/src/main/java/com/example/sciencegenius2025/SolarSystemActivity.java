package com.example.sciencegenius2025;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SolarSystemActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private TextView titleView, section1View, section2View, section3View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solar_system_fun);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Link TextViews from layout
        titleView = findViewById(R.id.title);
        section1View = findViewById(R.id.section1);
        section2View = findViewById(R.id.section2);
        section3View = findViewById(R.id.section3);


        // Fetch content from Firestore
        db.collection("chapters").document("solar_system")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        // Set text if fields are found
                        titleView.setText(snapshot.getString("title"));
                        section1View.setText(snapshot.getString("section1"));
                        section2View.setText(snapshot.getString("section2"));
                        section3View.setText(snapshot.getString("section3"));
                    } else {
                        Toast.makeText(this, "Chapter data not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load content: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

    }
}
