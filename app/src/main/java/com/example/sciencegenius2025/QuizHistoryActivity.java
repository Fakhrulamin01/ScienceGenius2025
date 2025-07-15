package com.example.sciencegenius2025;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private QuizHistoryAdapter adapter;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_history);

        recyclerView = findViewById(R.id.rvQuizHistory);
        progressBar = findViewById(R.id.progressBar);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setupRecyclerView();
        loadQuizHistory();

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // Closes the activity
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizHistoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }
    private void loadQuizHistory() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(ProgressBar.GONE);
            return;
        }
        String uid = currentUser.getUid();

        db.collection("quiz")
                .whereEqualTo("uid", uid)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(ProgressBar.GONE);

                    if (task.isSuccessful()) {
                        List<QuizHistoryItem> historyItems = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String quizName = document.getString("quizName");
                            Long score = document.getLong("score");
                            Timestamp timestampObj = document.getTimestamp("timestamp");

                            String timestamp = "";
                            if (timestampObj != null) {
                                Date date = timestampObj.toDate();
                                timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                            }

                            if (quizName != null && score != null && !timestamp.isEmpty()) {
                                historyItems.add(new QuizHistoryItem(quizName, score, timestamp));
                            }
                        }

                        if (historyItems.isEmpty()) {
                            Toast.makeText(QuizHistoryActivity.this,
                                    "No quiz history found", Toast.LENGTH_SHORT).show();
                        } else {
                            adapter.updateData(historyItems);

                            long bestScore = 0;
                            for (QuizHistoryItem item : historyItems) {
                                if (item.getScore() > bestScore) bestScore = item.getScore();
                            }
                            TextView summary = findViewById(R.id.tvSummary);
                            summary.setText("You've completed " + historyItems.size() + " quizzes! Best score: " + bestScore + "/10");
                            summary.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e("QuizHistory", "Error getting quiz documents: ", task.getException());
                        Toast.makeText(QuizHistoryActivity.this,
                                "Failed to load history: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
        showChapterProgress();

    }

    private void showChapterProgress() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        db.collection("progress")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Map<String, Object> data = snapshot.getData();
                        if (data != null) {
                            int completedCount = 0;
                            String[] chapters = {"human_body", "microorganism", "solar_system"};

                            for (String chapter : chapters) {
                                Map<String, Object> chapterStatus = (Map<String, Object>) data.get(chapter);
                                if (chapterStatus != null && Boolean.TRUE.equals(chapterStatus.get("completed"))) {
                                    completedCount++;
                                }
                            }

                            TextView chapterMsg = findViewById(R.id.tvChapterComplete);
                            chapterMsg.setVisibility(View.VISIBLE);

                            if (completedCount == chapters.length) {
                                chapterMsg.setText("🎉 You've completed all chapters!");
                            } else if (completedCount > 0) {
                                chapterMsg.setText("📘 You've completed " + completedCount + " out of " + chapters.length + " chapters!");
                            } else {
                                chapterMsg.setText("📖 Start reading chapters to track your progress!");
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("ChapterProgress", "Failed to fetch chapter progress", e));
    }


}