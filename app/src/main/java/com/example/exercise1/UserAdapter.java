package com.example.exercise1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quizappassignment2.R;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    private List<User> userList;
    private OnItemClickListener listener;

    public UserAdapter(List<User> userList, OnItemClickListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerviewitem, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewName;
        public TextView textViewEmail;
        public TextView textViewPassword;
        public TextView textViewAge;

        public UserViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewEmail = itemView.findViewById(R.id.textViewEmail);
            textViewPassword = itemView.findViewById(R.id.textViewPassword);
            textViewAge = itemView.findViewById(R.id.textViewAge);
        }

        public void bind(final User user, final OnItemClickListener listener) {
            textViewName.setText(user.getName());
            textViewEmail.setText(user.getEmail());
            textViewPassword.setText(user.getPassword());
            textViewAge.setText(String.valueOf(user.getAge()));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(user);
                }
            });
        }
    }
}
