package com.example.sciencegenius2025;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help); // Load the XML layout

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // Closes the activity
    }
}
