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

    public void updateLikes(int position, long likesCount) {
        DataSnapshot snapshot = quizSnapshots.get(position);
        snapshot.getRef().child("likes").setValue(likesCount);
        notifyItemChanged(position);
    }

    public class QuizViewHolder extends RecyclerView.ViewHolder {

        private TextView nameTextView;
        private TextView categoryTextView;
        private TextView difficultyTextView;
        private TextView startDateTextView;
        private TextView endDateTextView;
        private TextView likesTextView;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.quiznamerec);
            categoryTextView = itemView.findViewById(R.id.txtcategoryrec);
            difficultyTextView = itemView.findViewById(R.id.txtdifficultyrec);
            startDateTextView = itemView.findViewById(R.id.startdatetxt);
            endDateTextView = itemView.findViewById(R.id.enddatetxt);
            likesTextView = itemView.findViewById(R.id.liketxt);

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
            String name = quizSnapshot.child("name").getValue(String.class);
            String category = quizSnapshot.child("category").getValue(String.class);
            String difficulty = quizSnapshot.child("difficulty").getValue(String.class);
            String startDate = quizSnapshot.child("startDate").getValue(String.class);
            String endDate = quizSnapshot.child("endDate").getValue(String.class);
            Long likes = quizSnapshot.child("likes").getValue(Long.class);

            nameTextView.setText(name != null ? name : "N/A");
            categoryTextView.setText(category != null ? category : "N/A");
            difficultyTextView.setText(difficulty != null ? difficulty : "N/A");
            startDateTextView.setText(startDate != null ? startDate : "N/A");
            endDateTextView.setText(endDate != null ? endDate : "N/A");
            likesTextView.setText(String.valueOf(likes != null ? likes : 0));
        }
    }
}
