package com.example.sciencegenius2025;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizActivity extends AppCompatActivity {
    private TextView tvQuestion, tvProgress;
    private LinearLayout optionsContainer;
    private ProgressBar progressCorrect;
    private TextView tvCorrectProgress;


    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private List<Integer> userAnswers = new ArrayList<>();
    private FirebaseFirestore db;
    private String username = "";
    private String selectedTopic;
    private boolean answered = false;

    private MediaPlayer correctSound, wrongSound;

    String[] pastelColors = { "#81D4FA", "#81D4FA", "#81D4FA", "#81D4FA" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        tvQuestion = findViewById(R.id.tvQuestion);
        tvProgress = findViewById(R.id.tvProgress);
        optionsContainer = findViewById(R.id.optionsContainer);
        progressCorrect = findViewById(R.id.progressCorrect);
        tvCorrectProgress = findViewById(R.id.tvCorrectProgress);

        progressCorrect.setMax(10);
        progressCorrect.setProgress(0);
        tvCorrectProgress.setText("Correct: 0 / 10");


        db = FirebaseFirestore.getInstance();
        selectedTopic = getIntent().getStringExtra("topic");
        fetchUsernameFromFirestore();

        correctSound = MediaPlayer.create(this, R.raw.correct);
        wrongSound = MediaPlayer.create(this, R.raw.wrong);

        questionList = getQuestionsByTopic(selectedTopic);
        Collections.shuffle(questionList);
        loadQuestion(currentQuestionIndex);
    }

    private void fetchUsernameFromFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            username = doc.getString("username");
                        }
                    });
        }
    }

    private void loadQuestion(int index) {
        answered = false;
        Question q = questionList.get(index);
        tvQuestion.setText("Q" + (index + 1) + ". " + q.getQuestionText());
        tvProgress.setText("Question " + (index + 1) + " of " + questionList.size());

        optionsContainer.removeAllViews();

        for (int i = 0; i < q.getOptions().length; i++) {
            CardView card = new CardView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 16);
            card.setLayoutParams(params);
            card.setRadius(32);
            card.setCardElevation(8);
            card.setContentPadding(32, 32, 32, 32);
            card.setCardBackgroundColor(Color.parseColor(pastelColors[i % pastelColors.length]));

            TextView optionText = new TextView(this);
            optionText.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            optionText.setText(q.getOptions()[i]);
            optionText.setTextSize(20);
            optionText.setTypeface(ResourcesCompat.getFont(this, R.font.comic_font));
            optionText.setId(i);
            optionText.setTextColor(Color.BLACK);
            optionText.setPadding(24, 24, 24, 24);

            card.addView(optionText);

            card.setOnClickListener(v -> {
                if (!answered) {
                    v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(150)
                            .withEndAction(() -> {
                                v.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
                                TextView selectedOption = (TextView) ((CardView) v).getChildAt(0);
                                checkAnswer(selectedOption);
                            }).start();
                }
            });

            optionsContainer.addView(card);
        }
    }

    private void checkAnswer(TextView selectedOption) {
        answered = true;
        int selectedId = selectedOption.getId();
        Question q = questionList.get(currentQuestionIndex);
        userAnswers.add(selectedId);
        int correctIndex = q.getCorrectAnswerIndex();

        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            CardView card = (CardView) optionsContainer.getChildAt(i);
            TextView optionText = (TextView) card.getChildAt(0);

            if (i == correctIndex) {
                card.setCardBackgroundColor(Color.parseColor("#A5D6A7")); // Green
            } else if (i == selectedId) {
                card.setCardBackgroundColor(Color.parseColor("#EF9A9A")); // Red
            }

            card.setEnabled(false);
        }

        if (selectedId == correctIndex) {
            correctSound.start();
            score++;
            progressCorrect.setProgress(score);
            tvCorrectProgress.setText("Correct: " + score + " / 10");

        } else {
            wrongSound.start();
        }

        new Handler().postDelayed(() -> {
            currentQuestionIndex++;
            if (currentQuestionIndex < questionList.size()) {
                loadQuestion(currentQuestionIndex);
            } else {
                saveResultToFirestore();
            }
        }, 1500);
    }

    private void saveResultToFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("uid", user.getUid());
        result.put("username", username);
        result.put("score", score);
        result.put("quizName", selectedTopic);
        result.put("answers", userAnswers);
        result.put("timestamp", new Timestamp(new Date()));

        db.collection("quiz")
                .add(result)
                .addOnSuccessListener(doc -> {
                    Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
                    intent.putExtra("score", score);
                    intent.putExtra("topic", selectedTopic);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit quiz.", Toast.LENGTH_SHORT).show();
                });
    }

    private List<Question> getQuestionsByTopic(String topic) {
        List<Question> list = new ArrayList<>();
        switch (topic) {
            case "Humans":
                list.add(new Question("What organ pumps blood?", new String[]{"Lungs", "Kidney", "Heart", "Liver"}, 2));
                list.add(new Question("How many bones are in the adult body?", new String[]{"206", "208", "200", "210"}, 0));
                list.add(new Question("What do humans need to breathe?", new String[]{"Oxygen", "Carbon", "Hydrogen", "Nitrogen"}, 0));
                list.add(new Question("Where is your brain located?", new String[]{"Chest", "Arm", "Head", "Leg"}, 2));
                list.add(new Question("What system includes the heart and blood?", new String[]{"Digestive", "Circulatory", "Respiratory", "Nervous"}, 1));
                list.add(new Question("Which organ helps digest food?", new String[]{"Liver", "Stomach", "Heart", "Lung"}, 1));
                list.add(new Question("What is the largest organ in the human body?", new String[]{"Liver", "Skin", "Heart", "Brain"}, 1));
                list.add(new Question("Which body part helps you see?", new String[]{"Nose", "Eyes", "Ears", "Mouth"}, 1));
                list.add(new Question("What carries oxygen in the blood?", new String[]{"White cells", "Platelets", "Red cells", "Plasma"}, 2));
                list.add(new Question("Where are red blood cells made?", new String[]{"Skin", "Bone marrow", "Liver", "Heart"}, 1));
                list.add(new Question("Which sense helps you smell?", new String[]{"Touch", "Hearing", "Sight", "Smell"}, 3));
                list.add(new Question("How many lungs do humans have?", new String[]{"1", "2", "3", "4"}, 1));
                list.add(new Question("Which system controls body movement?", new String[]{"Digestive", "Skeletal", "Nervous", "Respiratory"}, 2));
                list.add(new Question("What is the job of the kidneys?", new String[]{"Pump blood", "Clean blood", "Store oxygen", "Digest food"}, 1));
                list.add(new Question("Which organ helps filter toxins?", new String[]{"Heart", "Lungs", "Liver", "Brain"}, 2));
                list.add(new Question("How many fingers on a hand?", new String[]{"4", "5", "6", "7"}, 1));
                list.add(new Question("What connects bones to muscles?", new String[]{"Veins", "Nerves", "Tendons", "Cartilage"}, 2));
                list.add(new Question("Which organ allows you to think?", new String[]{"Heart", "Brain", "Lung", "Liver"}, 1));
                list.add(new Question("Which part of the body bends?", new String[]{"Stomach", "Elbow", "Liver", "Tooth"}, 1));
                list.add(new Question("What do your ears help you do?", new String[]{"See", "Walk", "Hear", "Eat"}, 2));
                break;

            case "Microorganism":
                list.add(new Question("Which microorganism causes flu?", new String[]{"Bacteria", "Virus", "Fungus", "Algae"}, 1));
                list.add(new Question("What is used to kill bacteria?", new String[]{"Water", "Antibiotic", "Oil", "Juice"}, 1));
                list.add(new Question("Yeast is a type of...", new String[]{"Bacteria", "Virus", "Fungus", "Plant"}, 2));
                list.add(new Question("What helps make yogurt?", new String[]{"Bacteria", "Virus", "Fungus", "Insect"}, 0));
                list.add(new Question("Where do most microbes live?", new String[]{"Air", "Soil", "Plastic", "Fire"}, 1));
                list.add(new Question("Which microorganism is used in bread making?", new String[]{"Bacteria", "Virus", "Fungus", "Yeast"}, 3));
                list.add(new Question("Which microbe causes COVID-19?", new String[]{"Bacteria", "Fungus", "Virus", "Algae"}, 2));
                list.add(new Question("Fungi reproduce using...", new String[]{"Seeds", "Spores", "Eggs", "Roots"}, 1));
                list.add(new Question("Which is not a microorganism?", new String[]{"Algae", "Virus", "Bacteria", "Dog"}, 3));
                list.add(new Question("How can viruses spread?", new String[]{"Water only", "Air and touch", "Rock", "Soil"}, 1));
                list.add(new Question("Which microbe can clean oil spills?", new String[]{"Virus", "Fungus", "Bacteria", "Mold"}, 2));
                list.add(new Question("Which microorganism causes malaria?", new String[]{"Fungus", "Protozoa", "Virus", "Algae"}, 1));
                list.add(new Question("Microscope is used to see...", new String[]{"Stars", "Plants", "Microbes", "Books"}, 2));
                list.add(new Question("Which microbe lives in the human gut?", new String[]{"Fungus", "Bacteria", "Virus", "Mold"}, 1));
                list.add(new Question("What helps bacteria move?", new String[]{"Arms", "Legs", "Flagella", "Feet"}, 2));
                list.add(new Question("Which microbe causes athlete's foot?", new String[]{"Bacteria", "Fungus", "Virus", "Yeast"}, 1));
                list.add(new Question("Which microorganism helps clean water?", new String[]{"Yeast", "Virus", "Bacteria", "Mushroom"}, 2));
                list.add(new Question("Penicillin is made from...", new String[]{"Virus", "Fungus", "Algae", "Bacteria"}, 1));
                list.add(new Question("Probiotics contain...", new String[]{"Bad bacteria", "Helpful bacteria", "Fungus", "Virus"}, 1));
                list.add(new Question("Bacteria are usually...", new String[]{"Visible", "Invisible", "Loud", "Bright"}, 1));
                break;

            case "Solar System":
                list.add(new Question("Which planet is red?", new String[]{"Venus", "Mars", "Saturn", "Earth"}, 1));
                list.add(new Question("Which planet has rings?", new String[]{"Mars", "Earth", "Saturn", "Mercury"}, 2));
                list.add(new Question("Sun is a...", new String[]{"Planet", "Moon", "Star", "Asteroid"}, 2));
                list.add(new Question("Which planet is closest to the sun?", new String[]{"Venus", "Mercury", "Mars", "Earth"}, 1));
                list.add(new Question("How many planets are in our solar system?", new String[]{"8", "7", "9", "10"}, 0));
                list.add(new Question("Which planet is known as the 'blue planet'?", new String[]{"Earth", "Neptune", "Uranus", "Saturn"}, 0));
                list.add(new Question("Which planet is largest?", new String[]{"Earth", "Jupiter", "Venus", "Saturn"}, 1));
                list.add(new Question("Pluto is a...", new String[]{"Planet", "Star", "Dwarf Planet", "Moon"}, 2));
                list.add(new Question("Which planet is farthest from the Sun?", new String[]{"Earth", "Neptune", "Mars", "Saturn"}, 1));
                list.add(new Question("Which planet has the most moons?", new String[]{"Jupiter", "Saturn", "Earth", "Venus"}, 1));
                list.add(new Question("Earth orbits the sun in...", new String[]{"24 hours", "7 days", "30 days", "365 days"}, 3));
                list.add(new Question("What keeps planets in orbit?", new String[]{"Wind", "Gravity", "Water", "Sunlight"}, 1));
                list.add(new Question("Which planet is tilted and rolls?", new String[]{"Uranus", "Venus", "Neptune", "Earth"}, 0));
                list.add(new Question("Which planet spins the fastest?", new String[]{"Jupiter", "Mars", "Earth", "Saturn"}, 0));
                list.add(new Question("Moon is a...", new String[]{"Planet", "Satellite", "Asteroid", "Comet"}, 1));
                list.add(new Question("How many moons does Earth have?", new String[]{"1", "2", "3", "0"}, 0));
                list.add(new Question("Which planet is hottest?", new String[]{"Mercury", "Venus", "Mars", "Jupiter"}, 1));
                list.add(new Question("What is at the center of the solar system?", new String[]{"Earth", "Mars", "Sun", "Moon"}, 2));
                list.add(new Question("Which planet has the Great Red Spot?", new String[]{"Mars", "Venus", "Jupiter", "Saturn"}, 2));
                list.add(new Question("Saturn is mostly made of...", new String[]{"Rock", "Ice", "Gas", "Metal"}, 2));
                break;
        }
        Collections.shuffle(list);
        return list.subList(0, Math.min(list.size(), 10)); // return 10 random questions
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (correctSound != null) correctSound.release();
        if (wrongSound != null) wrongSound.release();
    }
}
