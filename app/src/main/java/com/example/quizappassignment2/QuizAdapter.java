package com.example.quizappassignment2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private List<String> questions;

    public void setQuestions(List<String> questions) {
        this.questions = questions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        String question = questions.get(position);
        holder.questionTextView.setText(question);
    }

    @Override
    public int getItemCount() {
        return (questions == null) ? 0 : questions.size();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView questionTextView;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.question_text);
        }
    }
}
