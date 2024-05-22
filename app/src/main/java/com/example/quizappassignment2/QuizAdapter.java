package com.example.quizappassignment2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private Context context;
    private List<DataSnapshot> quizSnapshots;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(DataSnapshot quizSnapshot);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public QuizAdapter(Context context, List<DataSnapshot> quizSnapshots) {
        this.context = context;
        this.quizSnapshots = quizSnapshots;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.quiz_recycler, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        DataSnapshot quizSnapshot = quizSnapshots.get(position);
        holder.bind(quizSnapshot);
    }

    @Override
    public int getItemCount() {
        return quizSnapshots.size();
    }

    public void setData(List<DataSnapshot> newData) {
        quizSnapshots.clear();
        quizSnapshots.addAll(newData);
        notifyDataSetChanged();
    }

    public class QuizViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;
        private TextView categoryTextView;
        private TextView difficultyTextView;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.rec_txt_name);
            categoryTextView = itemView.findViewById(R.id.rec_txt_category);
            difficultyTextView = itemView.findViewById(R.id.rec_txt_difficulty);

            // Set click listener for the entire item
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(quizSnapshots.get(position));
                        }
                    }
                }
            });
        }

        public void bind(DataSnapshot quizSnapshot) {
            // Assuming your Firebase data structure has fields like "name", "category", "difficulty"
            String name = (String) quizSnapshot.child("name").getValue();
            String category = (String) quizSnapshot.child("category").getValue();
            String difficulty = (String) quizSnapshot.child("difficulty").getValue();

            nameTextView.setText(name);
            categoryTextView.setText(category);
            difficultyTextView.setText(difficulty);
        }
    }
}
