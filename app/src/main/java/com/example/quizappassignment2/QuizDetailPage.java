package com.example.quizappassignment2;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class QuizDetailPage extends AppCompatActivity {

    private TextView txtName, txtCategory, txtDifficulty, txtLikes, txtStartDate, txtEndDate;
    private RecyclerView recyclerView;
    private QuizAdapter quizAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_quiz_detail_page);

        txtName = findViewById(R.id.txtnamequizdetailquizdetail);
        txtCategory = findViewById(R.id.txtcategoryquizdetail);
        txtDifficulty = findViewById(R.id.difficultyquizdetail);
        txtLikes = findViewById(R.id.likeamtquizdetail);
        txtStartDate = findViewById(R.id.quiz_detail_txt_start_date);
        txtEndDate = findViewById(R.id.quiz_detail_txt_end_date);
        recyclerView = findViewById(R.id.quiz_detail_recycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        quizAdapter = new QuizAdapter();
        recyclerView.setAdapter(quizAdapter);

        // Assume quizId is passed via Intent
        String quizId = getIntent().getStringExtra("QUIZ_ID");
        fetchQuizDetails(quizId);
    }

    private void fetchQuizDetails(String quizId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://your-backend-url.com") // Replace with your actual backend URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        QuizApi quizApi = retrofit.create(QuizApi.class);

        Call<Quiz> call = quizApi.getQuizDetails(quizId);
        call.enqueue(new Callback<Quiz>() {
            @Override
            public void onResponse(Call<Quiz> call, Response<Quiz> response) {
                if (response.isSuccessful()) {
                    Quiz quiz = response.body();
                    if (quiz != null) {
                        updateUI(quiz);
                    }
                } else {
                    Toast.makeText(QuizDetailPage.this, "Failed to load quiz details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Quiz> call, Throwable t) {
                Log.e("QuizDetailPage", "onFailure: ", t);
                Toast.makeText(QuizDetailPage.this, "Error loading quiz details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Quiz quiz) {
        txtName.setText(quiz.getName());
        txtCategory.setText(quiz.getCategory());
        txtDifficulty.setText(quiz.getDifficulty());
        txtLikes.setText(String.valueOf(quiz.getLikes()));
        txtStartDate.setText(quiz.getStartDate().toString());
        txtEndDate.setText(quiz.getEndDate().toString());

        // Assume Quiz has a list of questions
        quizAdapter.setQuestions(quiz.getQuestions());
    }

    interface QuizApi {
        @GET("/api/quizzes/{id}")
        Call<Quiz> getQuizDetails(@Path("id") String id);
    }

    class Quiz {
        private String name;
        private String category;
        private String difficulty;
        private int likes;
        private Date startDate;
        private Date endDate;
        private List<String> questions;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getDifficulty() { return difficulty; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public int getLikes() { return likes; }
        public void setLikes(int likes) { this.likes = likes; }
        public Date getStartDate() { return startDate; }
        public void setStartDate(Date startDate) { this.startDate = startDate; }
        public Date getEndDate() { return endDate; }
        public void setEndDate(Date endDate) { this.endDate = endDate; }
        public List<String> getQuestions() { return questions; }
        public void setQuestions(List<String> questions) { this.questions = questions; }
    }
}
