package com.example.quizappassignment2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class CreateQuizPage extends AppCompatActivity implements DateRangePickerDialog.OnDateRangeSelectedListener {

    private EditText editTextName;
    private TextInputLayout categoryInput, difficultyInput;
    private AutoCompleteTextView categoryDropdown, difficultyDropdown;
    private Button createButton, cancelButton, btnSelectStart, btnSelectEnd;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference quizzesReference;
    private DateRangePickerDialog dateRangePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        btnSelectStart = findViewById(R.id.btnSelectStart);
        btnSelectEnd = findViewById(R.id.btnSelectEnd);

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

        // Initialize and set up the date range picker dialog
        dateRangePickerDialog = new DateRangePickerDialog(this);
        dateRangePickerDialog.setOnDateRangeSelectedListener(this);

        // Set click listeners for the date range selection buttons
        btnSelectStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateRangePickerDialog.show();
            }
        });

        btnSelectEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateRangePickerDialog.show();
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

    @Override
    public void onStartDateSelected(Date startDate) {
        // Handle start date selection
        // You can display the selected start date or perform any other action
        Toast.makeText(this, "Start Date: " + startDate.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEndDateSelected(Date endDate) {
        // Handle end date selection
        // You can display the selected end date or perform any other action
        Toast.makeText(this, "End Date: " + endDate.toString(), Toast.LENGTH_SHORT).show();
    }
    private void createQuiz() {
        String name = editTextName.getText().toString();
        String category = categoryDropdown.getText().toString();
        String difficulty = difficultyDropdown.getText().toString();

        // Check if name, category, and difficulty are not empty
        if (name.isEmpty() || category.isEmpty() || difficulty.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the start and end dates are selected
        if (dateRangePickerDialog.getSelectedStartDate() == null || dateRangePickerDialog.getSelectedEndDate() == null) {
            Toast.makeText(this, "Please select start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected start and end dates
        Date startDate = dateRangePickerDialog.getSelectedStartDate();
        Date endDate = dateRangePickerDialog.getSelectedEndDate();

        // Calculate quiz time category
        String quizTimeCategory = calculateQuizTimeCategory(startDate, endDate);

        // Upload quiz data to Firebase
        String quizId = quizzesReference.push().getKey(); // Generate unique key for the quiz
        Quiz quiz = new Quiz(name, category, difficulty, startDate, endDate, quizTimeCategory);
        if (quizId != null) {
            quizzesReference.child(quizId).setValue(quiz)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(CreateQuizPage.this, "Quiz created successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CreateQuizPage.this, "Failed to create quiz", Toast.LENGTH_SHORT).show();
                            Log.e("CreateQuizPage", "Failed to create quiz", e);
                        }
                    });
        }
    }

    // Method to calculate quiz time category based on start and end dates
    private String calculateQuizTimeCategory(Date startDate, Date endDate) {
        Date currentDate = Calendar.getInstance().getTime();

        // If end date is before current date, it's previous
        if (endDate.before(currentDate)) {
            return "previous";
        }

        // If start date is after current date, it's upcoming
        if (startDate.after(currentDate)) {
            return "upcoming";
        }

        // If start date is before or equal to current date and end date is after current date, it's ongoing
        return "ongoing";
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
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    public class Quiz {
        private String name;
        private String category;
        private String difficulty;
        private Date startDate;
        private Date endDate;
        private String quizTimeCategory;

        public Quiz(String name, String category, String difficulty, Date startDate, Date endDate, String quizTimeCategory) {
            this.name = name;
            this.category = category;
            this.difficulty = difficulty;
            this.startDate = startDate;
            this.endDate = endDate;
            this.quizTimeCategory = quizTimeCategory;
        }

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

        public String getQuizTimeCategory() {
            return quizTimeCategory;
        }

        public void setQuizTimeCategory(String quizTimeCategory) {
            this.quizTimeCategory = quizTimeCategory;
        }

        // Method to calculate the quiz time category
        public void calculateQuizTimeCategory() {
            Date currentDate = new Date();

            if (endDate.after(currentDate)) {
                if (startDate.after(currentDate)) {
                    quizTimeCategory = "Upcoming";
                } else {
                    quizTimeCategory = "Ongoing";
                }
            } else {
                quizTimeCategory = "Previous";
            }
        }
    }
}