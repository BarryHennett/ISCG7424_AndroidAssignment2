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
    private DatabaseReference adminRef;
    private String quizId;
    private boolean hasLiked = false;
    private boolean isPlayable;
    private boolean isAdmin = false;

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
        adminRef = FirebaseDatabase.getInstance().getReference("admin");

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

                        // Check if the user has already liked the quiz
                        checkIfUserLiked();

                        // Check if the user is an admin
                        checkIfAdmin(); // Add this line

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
                if (hasLiked) {
                    unlikeQuiz();
                } else {
                    likeQuiz();
                }
            }
        });

        // Set up admin buttons (hidden by default)
        Button updateQuizButton = findViewById(R.id.updateQuizBTN);
        updateQuizButton.setVisibility(View.GONE);
        Button deleteQuizButton = findViewById(R.id.deleteQuizBTN);
        deleteQuizButton.setVisibility(View.GONE);
    }


    private void checkIfUserLiked() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current user's ID

        // Check if the user has already liked the quiz
        quizRef.child("likedBy").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Button likeQuizButton = findViewById(R.id.LikeQuizBTN);
                if (dataSnapshot.exists()) {
                    // User has already liked the quiz
                    hasLiked = true;
                    likeQuizButton.setText("Unlike");
                } else {
                    hasLiked = false;
                    likeQuizButton.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to check like status: " + databaseError.getMessage());
                Toast.makeText(QuizDetailPage.this, "Failed to check like status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfAdmin() {
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail(); // Get the current user's email

        // Compare the current user's email with the admin email
        if (userEmail != null && userEmail.equals("admin@email.com")) {
            isAdmin = true;
            setupAdminUI(); // Call setupAdminUI if the user is an admin
        } else {
            hideAdminUI(); // Hide the admin buttons if the user is not an admin
        }
    }

    private void hideAdminUI() {
        // Hide the like button for non-admin users
        Button likeQuizButton = findViewById(R.id.LikeQuizBTN);
        likeQuizButton.setVisibility(View.VISIBLE); // Show the like button
        // Hide admin-specific buttons
        Button updateQuizButton = findViewById(R.id.updateQuizBTN);
        updateQuizButton.setVisibility(View.GONE);
        Button deleteQuizButton = findViewById(R.id.deleteQuizBTN);
        deleteQuizButton.setVisibility(View.GONE);
    }


    private void setupAdminUI() {
        Log.d(TAG, "Setting up admin UI");
        Button likeQuizButton = findViewById(R.id.LikeQuizBTN);
        likeQuizButton.setVisibility(View.GONE);

        Button updateQuizButton = findViewById(R.id.updateQuizBTN);
        updateQuizButton.setVisibility(View.VISIBLE);
        updateQuizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QuizDetailPage.this, UpdateQuizPage.class);
                intent.putExtra("quizId", quizId);
                startActivity(intent);
            }
        });

        Button deleteQuizButton = findViewById(R.id.deleteQuizBTN);
        deleteQuizButton.setVisibility(View.VISIBLE);
        deleteQuizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteQuiz();
            }
        });
    }


    private void likeQuiz() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        quizRef.child("likedBy").child(userId).setValue(true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        quizRef.runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                Long currentLikes = mutableData.child("likes").getValue(Long.class);

                                if (currentLikes == null) {
                                    currentLikes = 0L;
                                }

                                Long updatedLikes = currentLikes + 1;

                                mutableData.child("likes").setValue(updatedLikes);

                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                                if (committed) {
                                    TextView likesTextView = findViewById(R.id.likequizdetail);
                                    long updatedLikes = dataSnapshot.child("likes").getValue(Long.class);
                                    likesTextView.setText(String.valueOf(updatedLikes));

                                    Toast.makeText(QuizDetailPage.this, "Liked!", Toast.LENGTH_SHORT).show();
                                    Button likeQuizButton = findViewById(R.id.LikeQuizBTN);
                                    likeQuizButton.setText("Unlike");
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

    private void unlikeQuiz() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the user's ID

        quizRef.child("likedBy").child(userId).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        quizRef.runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                Long currentLikes = mutableData.child("likes").getValue(Long.class);

                                if (currentLikes == null) {
                                    currentLikes = 0L;
                                }

                                Long updatedLikes = currentLikes - 1;

                                mutableData.child("likes").setValue(updatedLikes);

                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                                if (committed) {
                                    TextView likesTextView = findViewById(R.id.likequizdetail);
                                    long updatedLikes = dataSnapshot.child("likes").getValue(Long.class);
                                    likesTextView.setText(String.valueOf(updatedLikes));

                                    Toast.makeText(QuizDetailPage.this, "Unliked!", Toast.LENGTH_SHORT).show();
                                    Button likeQuizButton = findViewById(R.id.LikeQuizBTN);
                                    likeQuizButton.setText("Like");
                                    hasLiked = false;
                                } else {
                                    Toast.makeText(QuizDetailPage.this, "Failed to unlike quiz", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(QuizDetailPage.this, "Failed to unlike quiz", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteQuiz() {
        DatabaseReference quizRef = FirebaseDatabase.getInstance().getReference().child("quizzes").child(quizId);

        quizRef.removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(QuizDetailPage.this, "Quiz deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(QuizDetailPage.this, "Failed to delete quiz: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
