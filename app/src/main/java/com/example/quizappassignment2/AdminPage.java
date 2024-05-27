package com.example.quizappassignment2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class AdminPage extends AppCompatActivity implements QuizAdapter.OnItemClickListener {

    private RecyclerView recyclerViewQuizzes;
    private QuizAdapter quizAdapter;
    private List<DataSnapshot> quizSnapshots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_page);

        recyclerViewQuizzes = findViewById(R.id.adminrecyclerview);
        quizSnapshots = new ArrayList<>();
        quizAdapter = new QuizAdapter(this, quizSnapshots);
        recyclerViewQuizzes.setAdapter(quizAdapter);
        recyclerViewQuizzes.setLayoutManager(new LinearLayoutManager(this));

        // Set up Firebase listener to update quiz data
        DatabaseReference quizzesRef = FirebaseDatabase.getInstance().getReference("quizzes");
        quizzesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quizSnapshots.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    quizSnapshots.add(snapshot);
                }
                quizAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });

        // Set item click listener for RecyclerView
        quizAdapter.setOnItemClickListener(this);

        Button signOutButton = findViewById(R.id.signoutbtnadmin);
        Button addQuizButton = findViewById(R.id.admincreatequizbtn);

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(AdminPage.this, LoginPage.class));
                finish();
            }
        });

        addQuizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminPage.this, CreateQuizPage.class));
            }
        });
    }

    // Handle item click for RecyclerView
    @Override
    public void onItemClick(DataSnapshot quizSnapshot) {
        // Handle item click here
        // For example, navigate to the details page
        Intent intent = new Intent(AdminPage.this, QuizDetailPage.class);
        intent.putExtra("quizId", quizSnapshot.getKey());
        startActivity(intent);
    }

}
