package com.example.quizappassignment2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class QuizDetailPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_detail_page);

        // Retrieve data from intent extras
        String name = getIntent().getStringExtra("name");
        String category = getIntent().getStringExtra("category");
        String difficulty = getIntent().getStringExtra("difficulty");
        // Add additional fields as necessary
        String likes = getIntent().getStringExtra("likes");
        String startDate = getIntent().getStringExtra("start_date");
        String endDate = getIntent().getStringExtra("end_date");

        // Display quiz details
        TextView nameTextView = findViewById(R.id.txtnamequizdetailquizdetail);
        nameTextView.setText(name);

        TextView categoryTextView = findViewById(R.id.txtcategoryquizdetail);
        categoryTextView.setText(category);

        TextView difficultyTextView = findViewById(R.id.difficultyquizdetail);
        difficultyTextView.setText(difficulty);

        TextView likesTextView = findViewById(R.id.likeamtquizdetail);
        likesTextView.setText(likes);

        TextView startDateTextView = findViewById(R.id.quiz_detail_txt_start_date);
        startDateTextView.setText(startDate);

        TextView endDateTextView = findViewById(R.id.quiz_detail_txt_end_date);
        endDateTextView.setText(endDate);

        // Set click listener for the return button
        Button returnButton = findViewById(R.id.quizdetailreturnbtn);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous activity
                finish();
            }
        });
        Button playQuizButton = findViewById(R.id.Playquizbtn);


        playQuizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the PlayQuizPage activity
                startActivity(new Intent(QuizDetailPage.this, PlayQuizPage.class));
            }
        });

    }
}
