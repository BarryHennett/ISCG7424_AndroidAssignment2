package com.example.quizappassignment2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class QuizDetailPage extends AppCompatActivity {

    private static final String TAG = "QuizDetailPage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_detail_page);

        // Retrieve quiz ID from intent extras
        String quizId = getIntent().getStringExtra("quizId");

        // Retrieve quiz details from Firebase
        DatabaseReference quizRef = FirebaseDatabase.getInstance().getReference("quizzes").child(quizId);
        quizRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        // Retrieve data from dataSnapshot and set TextViews
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String category = dataSnapshot.child("category").getValue(String.class);
                        String difficulty = dataSnapshot.child("difficulty").getValue(String.class);
                        String likes = dataSnapshot.child("likes").getValue(String.class);
                        String startDate = dataSnapshot.child("start_date").getValue(String.class);
                        String endDate = dataSnapshot.child("end_date").getValue(String.class);

                        // Format start date
                        String formattedStartDate = formatDate(startDate);

                        // Format end date
                        String formattedEndDate = formatDate(endDate);

                        // Display quiz details
                        TextView nameTextView = findViewById(R.id.txtnamequizdetailquizdetail);
                        TextView categoryTextView = findViewById(R.id.txtcategoryquizdetail);
                        TextView difficultyTextView = findViewById(R.id.difficultyquizdetail);
                        TextView likesTextView = findViewById(R.id.likequizdetail);
                        TextView startDateTextView = findViewById(R.id.detailstartdate);
                        TextView endDateTextView = findViewById(R.id.detailenddate);

                        if (nameTextView != null && categoryTextView != null && difficultyTextView != null &&
                                likesTextView != null && startDateTextView != null && endDateTextView != null) {
                            nameTextView.setText(name);
                            categoryTextView.setText(category);
                            difficultyTextView.setText(difficulty);
                            likesTextView.setText(likes);
                            startDateTextView.setText(formattedStartDate);
                            endDateTextView.setText(formattedEndDate);
                        } else {
                            Log.e(TAG, "One or more TextViews is null");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error retrieving quiz details: " + e.getMessage());
                        Toast.makeText(QuizDetailPage.this, "Failed to load quiz details", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    // Quiz with given ID doesn't exist
                    Toast.makeText(QuizDetailPage.this, "Quiz not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                Log.e(TAG, "Failed to read quiz details: " + databaseError.getMessage());
                Toast.makeText(QuizDetailPage.this, "Failed to load quiz details", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Set click listener for the return button
        Button returnButton = findViewById(R.id.quizdetailreturnbtn);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous activity
                finish();
            }
        });

        // Set click listener for the Play Quiz button
        Button playQuizButton = findViewById(R.id.Playquizbtn);
        playQuizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the PlayQuizPage activity
                startActivity(new Intent(QuizDetailPage.this, PlayQuizPage.class));
            }
        });
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e(TAG, "Error formatting date: " + e.getMessage());
            return dateString; // Return original date string if parsing fails
        }
    }
}
