package com.example.quizappassignment2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class QuizDetailPage extends AppCompatActivity {

    private static final String TAG = "QuizDetailPage";
    private DatabaseReference quizRef;
    private String quizId;
    private boolean hasLiked = false;
    private boolean isPlayable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_detail_page);

        // Retrieve quiz ID and playability flag from intent extras
        quizId = getIntent().getStringExtra("quizId");
        isPlayable = getIntent().getBooleanExtra("isPlayable", false);

        // Retrieve the score if available
        int score = getIntent().getIntExtra("score", -1);

        if (quizId == null) {
            Log.e(TAG, "Quiz ID is null");
            Toast.makeText(this, "Invalid quiz ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Retrieve quiz details from Firebase
        quizRef = FirebaseDatabase.getInstance().getReference("quizzes").child(quizId);

        quizRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        // Retrieve data from dataSnapshot and set TextViews
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String category = dataSnapshot.child("category").getValue(String.class);
                        String difficulty = dataSnapshot.child("difficulty").getValue(String.class);
                        Long likes = dataSnapshot.child("likes").getValue(Long.class);
                        String startDate = dataSnapshot.child("startDate").getValue(String.class);
                        String endDate = dataSnapshot.child("endDate").getValue(String.class);

                        // Check if any of the required fields are null
                        if (name == null || category == null || difficulty == null || likes == null || startDate == null || endDate == null) {
                            Log.e(TAG, "One or more required fields are null");
                            Toast.makeText(QuizDetailPage.this, "Incomplete quiz details", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        // Display quiz details
                        TextView nameTextView = findViewById(R.id.txtnamequizdetailquizdetail);
                        TextView categoryTextView = findViewById(R.id.txtcategoryquizdetail);
                        TextView difficultyTextView = findViewById(R.id.difficultyquizdetail);
                        TextView likesTextView = findViewById(R.id.likequizdetail);
                        TextView startDateTextView = findViewById(R.id.detailstartdate);
                        TextView endDateTextView = findViewById(R.id.detailenddate);

                        nameTextView.setText(name);
                        categoryTextView.setText(category);
                        difficultyTextView.setText(difficulty);
                        likesTextView.setText(String.valueOf(likes));
                        startDateTextView.setText(startDate);
                        endDateTextView.setText(endDate);

                        // Update the score if available
                        if (score >= 0) {
                            TextView scoreTextView = findViewById(R.id.detailscore);
                            scoreTextView.setText(score + "/10");
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
        if (isPlayable) {
            playQuizButton.setVisibility(View.VISIBLE);
            playQuizButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start the PlayQuizPage activity and pass the quiz ID
                    Intent intent = new Intent(QuizDetailPage.this, PlayQuizPage.class);
                    intent.putExtra("quizId", quizId);
                    startActivity(intent);
                }
            });
        } else {
            playQuizButton.setVisibility(View.GONE);
        }

        // Set click listener for the Like Quiz button
        Button likeQuizButton = findViewById(R.id.LikeQuizBTN);
        likeQuizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasLiked) {
                    likeQuiz();
                }
            }
        });
    }

    private void likeQuiz() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current user's ID

        // Check if the user has already liked the quiz
        quizRef.child("likedBy").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User has already liked the quiz
                    Toast.makeText(QuizDetailPage.this, "You have already liked this quiz", Toast.LENGTH_SHORT).show();
                    // Set the button as selected and disable it
                    Button likeQuizButton = findViewById(R.id.LikeQuizBTN);
                    likeQuizButton.setSelected(true);
                    likeQuizButton.setEnabled(false);
                } else {
                    // Update the likedBy list in the database
                    quizRef.child("likedBy").child(userId).setValue(true)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Increment the likes count using a transaction
                                    quizRef.runTransaction(new Transaction.Handler() {
                                        @NonNull
                                        @Override
                                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                            // Retrieve the current likes count
                                            Long currentLikes = mutableData.child("likes").getValue(Long.class);

                                            // If the likes count is null, initialize it to 0
                                            if (currentLikes == null) {
                                                currentLikes = 0L;
                                            }

                                            // Increment the likes count by 1
                                            Long updatedLikes = currentLikes + 1;

                                            // Update the likes count in the database
                                            mutableData.child("likes").setValue(updatedLikes);

                                            // Return success
                                            return Transaction.success(mutableData);
                                        }

                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                                            if (committed) {
                                                // Update the likes count TextView
                                                TextView likesTextView = findViewById(R.id.likequizdetail);
                                                long updatedLikes = dataSnapshot.child("likes").getValue(Long.class);
                                                likesTextView.setText(String.valueOf(updatedLikes));

                                                Toast.makeText(QuizDetailPage.this, "Liked!", Toast.LENGTH_SHORT).show();
                                                // Set the button as selected and disable it
                                                Button likeQuizButton = findViewById(R.id.LikeQuizBTN);
                                                likeQuizButton.setSelected(true);
                                                likeQuizButton.setEnabled(false);
                                                hasLiked = true;
                                            } else {
                                                Toast.makeText(QuizDetailPage.this, "Failed to like quiz", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(QuizDetailPage.this, "Failed to like quiz", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QuizDetailPage.this, "Failed to check like status", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
