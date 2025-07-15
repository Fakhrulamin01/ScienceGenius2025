package com.example.sciencegenius2025;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HumanBodyActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private TextView titleView, section1View, section2View, section3View, section4View, section5View;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_human);

        Button backButton = findViewById(R.id.backButton);
        Button nextButton = findViewById(R.id.nextButton);

        backButton.setOnClickListener(v -> finish());

        nextButton.setOnClickListener(v -> {
            // Mark the chapter as complete
            markChapterAsComplete("human_body");

            // Go to the next chapter
            Intent intent = new Intent(HumanBodyActivity.this, MicroorganismActivity.class);
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
        section5View = findViewById(R.id.section5);


        // Fetch content from Firestore
        db.collection("chapters").document("human_body")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        // Set text if fields are found
                        titleView.setText(snapshot.getString("title"));
                        section1View.setText(snapshot.getString("section1"));
                        section2View.setText(snapshot.getString("section2"));
                        section3View.setText(snapshot.getString("section3"));
                        section4View.setText(snapshot.getString("section4"));
                        section5View.setText(snapshot.getString("section5"));
                    } else {
                        Toast.makeText(this, "Chapter data not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load content: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

    }
    private void markChapterAsComplete(String chapterId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        Map<String, Object> chapterProgress = new HashMap<>();
        chapterProgress.put("completed", true);
        chapterProgress.put("timestamp", new com.google.firebase.Timestamp(new java.util.Date()));

        Map<String, Object> data = new HashMap<>();
        data.put(chapterId, chapterProgress);

        FirebaseFirestore.getInstance()
                .collection("progress")
                .document(uid)
                .set(data, SetOptions.merge());
    }


}
