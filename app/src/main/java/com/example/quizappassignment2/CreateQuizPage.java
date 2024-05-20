package com.example.quizappassignment2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Date;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class CreateQuizPage extends AppCompatActivity {

    private EditText editTextName;
    private TextInputLayout categoryInput, difficultyInput;
    private Button createButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_quiz_page);

        editTextName = findViewById(R.id.editTextnamecreatequiz);
        categoryInput = findViewById(R.id.categorycreatequiz);
        difficultyInput = findViewById(R.id.create_quiz_txt_difficulty);
        createButton = findViewById(R.id.makebtncreatequiz);
        cancelButton = findViewById(R.id.cancelcreatequiz);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createQuiz();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void createQuiz() {
        String name = editTextName.getText().toString();
        String category = categoryInput.getEditText().getText().toString();
        String difficulty = difficultyInput.getEditText().getText().toString();
        Date date = new Date();

        Quiz quiz = new Quiz(name, category, difficulty, date);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://your-backend-url.com") // Replace with your actual backend URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        QuizApi quizApi = retrofit.create(QuizApi.class);

        Call<Quiz> call = quizApi.createQuiz(quiz);
        call.enqueue(new Callback<Quiz>() {
            @Override
            public void onResponse(Call<Quiz> call, Response<Quiz> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateQuizPage.this, "Quiz created successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CreateQuizPage.this, "Failed to create quiz!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Quiz> call, Throwable t) {
                Log.e("CreateQuizPage", "onFailure: ", t);
                Toast.makeText(CreateQuizPage.this, "Error creating quiz", Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface QuizApi {
        @POST("/api/quizzes/create")
        Call<Quiz> createQuiz(@Body Quiz quiz);
    }

    class Quiz {
        private String name;
        private String category;
        private String difficulty;
        private Date date;

        public Quiz(String name, String category, String difficulty, Date date) {
            this.name = name;
            this.category = category;
            this.difficulty = difficulty;
            this.date = date;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }
    }
}
