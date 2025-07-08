package com.example.sciencegenius2025;

public class QuizHistoryItem {
    private String quizName;
    private long score;
    private String timestamp;

    public QuizHistoryItem(String quizName, long score, String timestamp) {
        this.quizName = quizName;
        this.score = score;
        this.timestamp = timestamp;
    }

    public String getQuizName() {
        return quizName;
    }

    public long getScore() {
        return score;
    }

    public String getTimestamp() {
        return timestamp;
    }
}