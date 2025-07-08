package com.example.sciencegenius2025;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class QuizHistoryAdapter extends RecyclerView.Adapter<QuizHistoryAdapter.QuizHistoryViewHolder> {

    private List<QuizHistoryItem> historyItems;

    public QuizHistoryAdapter(List<QuizHistoryItem> historyItems) {
        this.historyItems = historyItems;
    }

    @NonNull
    @Override
    public QuizHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quiz_history, parent, false);
        return new QuizHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizHistoryViewHolder holder, int position) {
        QuizHistoryItem item = historyItems.get(position);

        holder.tvQuizName.setText(item.getQuizName());
        holder.tvScore.setText("Score: " + item.getScore() + "/10"); // Assuming max score is 3
        holder.tvDate.setText(item.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    public void updateData(List<QuizHistoryItem> newItems) {
        historyItems = newItems;
        notifyDataSetChanged();
    }

    static class QuizHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuizName, tvScore, tvDate;

        public QuizHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuizName = itemView.findViewById(R.id.tvQuizName);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
