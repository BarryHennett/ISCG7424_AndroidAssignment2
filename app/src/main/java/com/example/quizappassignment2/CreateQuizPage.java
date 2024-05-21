package com.example.quizappassignment2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Date;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class CreateQuizPage extends AppCompatActivity {

    private EditText editTextName;
    private TextInputLayout categoryInput, difficultyInput;
    private AutoCompleteTextView categoryDropdown, difficultyDropdown;
    private Button createButton, cancelButton;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference quizzesReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_quiz_page);

        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        quizzesReference = firebaseDatabase.getReference("quizzes");

        editTextName = findViewById(R.id.editTextnamecreatequiz);
        categoryInput = findViewById(R.id.categorycreatequiz);
        difficultyInput = findViewById(R.id.difficultycreatequiz);
        categoryDropdown = findViewById(R.id.autoCompleteCategory);
        difficultyDropdown = findViewById(R.id.autoCompleteDifficulty);
        createButton = findViewById(R.id.makebtncreatequiz);
        cancelButton = findViewById(R.id.cancelcreatequiz);

        setupDifficultyDropdown();
        fetchCategoriesFromApi();

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

    private void setupDifficultyDropdown() {
        String[] difficulties = new String[]{"easy", "medium", "hard"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, difficulties);
        difficultyDropdown.setAdapter(adapter);
    }

    private void fetchCategoriesFromApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://opentdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        QuizApi quizApi = retrofit.create(QuizApi.class);

        Call<CategoryResponse> call = quizApi.getCategories();
        call.enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                if (response.isSuccessful()) {
                    List<Category> categories = response.body().getTriviaCategories();
                    populateCategoryDropdown(categories);
                } else {
                    Toast.makeText(CreateQuizPage.this, "Failed to fetch categories!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                Log.e("CreateQuizPage", "onFailure: ", t);
                Toast.makeText(CreateQuizPage.this, "Error fetching categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateCategoryDropdown(List<Category> categories) {
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        categoryDropdown.setAdapter(adapter);
    }

    private void createQuiz() {
        String name = editTextName.getText().toString();
        String category = categoryDropdown.getText().toString();
        String difficulty = difficultyDropdown.getText().toString();
        Date date = new Date();

        Quiz quiz = new Quiz(name, category, difficulty, date);

        // Store the quiz in Firebase Realtime Database
        quizzesReference.push().setValue(quiz).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CreateQuizPage.this, "Quiz created successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CreateQuizPage.this, "Failed to create quiz!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    interface QuizApi {
        @GET("api_category.php")
        Call<CategoryResponse> getCategories();
    }

    class CategoryResponse {
        private List<Category> trivia_categories;

        public List<Category> getTriviaCategories() {
            return trivia_categories;
        }

        public void setTriviaCategories(List<Category> trivia_categories) {
            this.trivia_categories = trivia_categories;
        }
    }

    class Category {
        private int id;
        private String name;

        @Override
        public String toString() {
            return name;
        }

        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
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
