package com.example.sciencegenius2025;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class QuizHistoryAdapter extends RecyclerView.Adapter<QuizHistoryAdapter.QuizHistoryViewHolder> {

    private List<QuizHistoryItem> historyItems;
    private Context context;

    public QuizHistoryAdapter(List<QuizHistoryItem> historyItems) {
        this.historyItems = historyItems;
    }

    @NonNull
    @Override
    public QuizHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_quiz_history, parent, false);
        return new QuizHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizHistoryViewHolder holder, int position) {
        QuizHistoryItem item = historyItems.get(position);

        holder.tvQuizName.setText(item.getQuizName());
        holder.tvScore.setText("Score: " + item.getScore() + "/10");
        holder.tvDate.setText(item.getTimestamp());

        // Badge icon (optional: use emojis instead)
        holder.tvQuizName.setCompoundDrawablesWithIntrinsicBounds(0, 0, getBadgeRes(item.getScore()), 0);

        // Score text color by performance
        int color = getColorForScore(item.getScore());
        holder.tvScore.setTextColor(color);
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

    private int getBadgeRes(long score) {
        if (score >= 9) return R.drawable.ic_gold_medal;     // 🥇 gold
        if (score >= 7) return R.drawable.ic_silver_medal;   // 🥈 silver
        if (score >= 5) return R.drawable.ic_bronze_medal;   // 🥉 bronze
        return R.drawable.ic_try_again;                      // 😅 try again
    }

    private int getColorForScore(long score) {
        if (score >= 9) return 0xFF2E7D32; // green
        if (score >= 7) return 0xFF1976D2; // blue
        if (score >= 5) return 0xFFFFA000; // orange
        return 0xFFD32F2F; // red
    }
}
