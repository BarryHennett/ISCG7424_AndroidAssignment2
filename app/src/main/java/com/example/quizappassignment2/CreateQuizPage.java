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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class CreateQuizPage extends AppCompatActivity implements DateRangePickerDialog.OnDateRangeSelectedListener {

    private EditText editTextName;
    private TextInputLayout categoryInput, difficultyInput;
    private AutoCompleteTextView categoryDropdown, difficultyDropdown;
    private Button createButton, cancelButton, btnSelectStart, btnSelectEnd;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference quizzesReference;
    private DateRangePickerDialog dateRangePickerDialog;
    private Map<String, Integer> categoryMap;

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

        dateRangePickerDialog = new DateRangePickerDialog(this);
        dateRangePickerDialog.setOnDateRangeSelectedListener(this);

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

        categoryMap = new HashMap<>();
        for (Category category : categories) {
            categoryMap.put(category.getName(), category.getId());
        }
    }

    @Override
    public void onStartDateSelected(Date startDate) {
        Toast.makeText(this, "Start Date: " + startDate.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEndDateSelected(Date endDate) {
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

        // Check start and end dates are selected
        if (dateRangePickerDialog.getSelectedStartDate() == null || dateRangePickerDialog.getSelectedEndDate() == null) {
            Toast.makeText(this, "Please select start and end dates", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get selected start and end dates
        Date startDate = dateRangePickerDialog.getSelectedStartDate();
        Date endDate = dateRangePickerDialog.getSelectedEndDate();

        // Get quiz time category
        String quizTimeCategory = calculateQuizTimeCategory(startDate, endDate);

        // Get Key for the quiz
        String quizId = quizzesReference.push().getKey();

        // Fetch questions from API
        fetchQuestionsFromApi(name, category, difficulty, quizTimeCategory, startDate, endDate, quizId);
    }

    private void fetchQuestionsFromApi(String name, String category, String difficulty, String quizTimeCategory, Date startDate, Date endDate, String quizId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://opentdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        QuizApi quizApi = retrofit.create(QuizApi.class);

        Integer categoryId = categoryMap.get(category);
        Call<QuestionResponse> call;
        if (categoryId != null) {
            call = quizApi.getQuestions(10, difficulty, categoryId, "multiple");
        } else {
            call = quizApi.getQuestions(10, difficulty, null, "multiple");
        }
        call.enqueue(new Callback<QuestionResponse>() {
            @Override
            public void onResponse(Call<QuestionResponse> call, Response<QuestionResponse> response) {
                if (response.isSuccessful()) {
                    List<Question> questions = response.body().getResults();
                    uploadQuizToFirebase(name, category, difficulty, startDate, endDate, quizTimeCategory, questions, quizId);
                } else {
                    Toast.makeText(CreateQuizPage.this, "Failed to fetch questions!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<QuestionResponse> call, Throwable t) {
                Log.e("CreateQuizPage", "onFailure: ", t);
                Toast.makeText(CreateQuizPage.this, "Error fetching questions", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void uploadQuizToFirebase(String name, String category, String difficulty, Date startDate, Date endDate, String quizTimeCategory, List<Question> questions, String quizId) {
        // Format start date as DD/MM/YYYY
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedStartDate = dateFormat.format(startDate);

        // Format end date as DD/MM/YYYY
        String formattedEndDate = dateFormat.format(endDate);

        Quiz quiz = new Quiz(name, category, difficulty, formattedStartDate, formattedEndDate, quizTimeCategory);

        if (quizId != null) {
            quizzesReference.child(quizId).setValue(quiz)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            uploadQuestionsToFirebase(quizId, questions);
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


    private void uploadQuestionsToFirebase(String quizId, List<Question> questions) {
        DatabaseReference questionsRef = quizzesReference.child(quizId).child("questions");
        for (Question question : questions) {
            String questionId = questionsRef.push().getKey();
            if (questionId != null) {
                questionsRef.child(questionId).setValue(question);
            }
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

        @GET("api.php")
        Call<QuestionResponse> getQuestions(@Query("amount") int amount, @Query("difficulty") String difficulty, @Query("category") Integer category, @Query("type") String type);
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

    class QuestionResponse {
        private List<Question> results;

        public List<Question> getResults() {
            return results;
        }

        public void setResults(List<Question> results) {
            this.results = results;
        }
    }

    class Category {
        private int id;
        private String name;

        @Override
        public String toString() {
            return name;
        }

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

    class Question {
        private String question;
        private List<String> incorrect_answers;
        private String correct_answer;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public List<String> getIncorrectAnswers() {
            return incorrect_answers;
        }

        public void setIncorrectAnswers(List<String> incorrect_answers) {
            this.incorrect_answers = incorrect_answers;
        }

        public String getCorrectAnswer() {
            return correct_answer;
        }

        public void setCorrectAnswer(String correct_answer) {
            this.correct_answer = correct_answer;
        }
    }

    class Quiz {
        private String name;
        private String category;
        private String difficulty;
        private String startDate;
        private String endDate;
        private String quizTimeCategory;
        private int likes;
        private List<String> likedBy; // List of user IDs who liked the quiz

        public Quiz(String name, String category, String difficulty, String startDate, String endDate, String quizTimeCategory) {
            this.name = name;
            this.category = category;
            this.difficulty = difficulty;
            this.startDate = startDate;
            this.endDate = endDate;
            this.quizTimeCategory = quizTimeCategory;
            this.likes = 0; // Initialize likes to 0
            this.likedBy = new ArrayList<>(); // Initialize the list of likedBy
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

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getQuizTimeCategory() {
            return quizTimeCategory;
        }

        public void setQuizTimeCategory(String quizTimeCategory) {
            this.quizTimeCategory = quizTimeCategory;
        }

        public int getLikes() {
            return likes;
        }

        public void setLikes(int likes) {
            this.likes = likes;
        }

        public List<String> getLikedBy() {
            return likedBy;
        }

        public void setLikedBy(List<String> likedBy) {
            this.likedBy = likedBy;
        }

        // Method to add a user to the likedBy list
        public void addLikedBy(String userId) {
            this.likedBy.add(userId);
        }
    }
}