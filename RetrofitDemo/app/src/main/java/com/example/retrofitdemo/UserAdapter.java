package com.example.retrofitdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rvuseritem, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvId.setText("ID: " + user.getId());
        holder.tvEmail.setText("Email: " + user.getEmail());
        holder.tvFName.setText("First Name: " + user.getFirst_name());
        holder.tvLName.setText("Last Name: " + user.getLast_name());

        Glide.with(holder.itemView)
                .load(user.getAvatar())
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .into(holder.imgvw);    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imgvw;
        TextView tvId, tvEmail, tvFName, tvLName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgvw = itemView.findViewById(R.id.imgvw);
            tvId = itemView.findViewById(R.id.tvid);
            tvEmail = itemView.findViewById(R.id.tvemail);
            tvFName = itemView.findViewById(R.id.tvfname);
            tvLName = itemView.findViewById(R.id.tvlname);
        }
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }
}
