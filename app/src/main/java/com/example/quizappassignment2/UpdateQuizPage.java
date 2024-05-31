package com.example.quizappassignment2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
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

public class UpdateQuizPage extends AppCompatActivity implements DateRangePickerDialog.OnDateRangeSelectedListener {

    private EditText editTextnamecreatequiz;
    private AutoCompleteTextView autoCompleteCategory, autoCompleteDifficulty;
    private Button btnSelectStart, btnSelectEnd, updatequizbtn, cancelcreatequiz;
    private DatabaseReference quizzesRef;
    private String quizId;
    private Date selectedStartDate, selectedEndDate;
    private Map<String, Integer> categoryMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_quiz_page);

        editTextnamecreatequiz = findViewById(R.id.editTextnamecreatequiz);
        autoCompleteCategory = findViewById(R.id.autoCompleteCategory);
        autoCompleteDifficulty = findViewById(R.id.autoCompleteDifficulty);
        btnSelectStart = findViewById(R.id.btnSelectStart);
        btnSelectEnd = findViewById(R.id.btnSelectEnd);
        updatequizbtn = findViewById(R.id.updatequizbtn);
        cancelcreatequiz = findViewById(R.id.cancelcreatequiz);

        quizId = getIntent().getStringExtra("quizId");
        if (quizId == null) {
            Toast.makeText(this, "Invalid quiz ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        quizzesRef = FirebaseDatabase.getInstance().getReference().child("quizzes");

        fetchCategoriesFromApi();
        setupDifficultyDropdown();

        quizzesRef.child(quizId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String category = dataSnapshot.child("category").getValue(String.class);
                    String difficulty = dataSnapshot.child("difficulty").getValue(String.class);
                    String startDate = dataSnapshot.child("startDate").getValue(String.class);
                    String endDate = dataSnapshot.child("endDate").getValue(String.class);

                    editTextnamecreatequiz.setText(name);
                    autoCompleteCategory.setText(category, false);
                    autoCompleteDifficulty.setText(difficulty, false);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    try {
                        if (startDate != null) {
                            selectedStartDate = sdf.parse(startDate);
                        }
                        if (endDate != null) {
                            selectedEndDate = sdf.parse(endDate);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(UpdateQuizPage.this, "Quiz not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UpdateQuizPage.this, "Error loading quiz: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        btnSelectStart.setOnClickListener(v -> launchDateRangePickerDialog(true));
        btnSelectEnd.setOnClickListener(v -> launchDateRangePickerDialog(false));

        updatequizbtn.setOnClickListener(v -> updateQuiz());

        cancelcreatequiz.setOnClickListener(v -> {
            Intent intent = new Intent(UpdateQuizPage.this, AdminPage.class);
            startActivity(intent);
        });
    }

    private void launchDateRangePickerDialog(boolean isStartDate) {
        DateRangePickerDialog dateRangePickerDialog = new DateRangePickerDialog(UpdateQuizPage.this);
        dateRangePickerDialog.setOnDateRangeSelectedListener(this);
        dateRangePickerDialog.show();
    }

    @Override
    public void onStartDateSelected(Date startDate) {
        selectedStartDate = startDate;
    }

    @Override
    public void onEndDateSelected(Date endDate) {
        selectedEndDate = endDate;
    }

    private void updateQuiz() {
        String updatedName = editTextnamecreatequiz.getText().toString();
        String updatedCategory = autoCompleteCategory.getText().toString();
        String updatedDifficulty = autoCompleteDifficulty.getText().toString();

        DatabaseReference quizRef = FirebaseDatabase.getInstance().getReference().child("quizzes").child(quizId);

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", updatedName);
        updatedData.put("category", updatedCategory);
        updatedData.put("difficulty", updatedDifficulty);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        if (selectedStartDate != null) {
            String formattedStartDate = dateFormat.format(selectedStartDate);
            updatedData.put("startDate", formattedStartDate);
        }

        if (selectedEndDate != null) {
            String formattedEndDate = dateFormat.format(selectedEndDate);
            updatedData.put("endDate", formattedEndDate);
        }

        String updatedQuizTimeCategory = calculateQuizTimeCategory(selectedStartDate, selectedEndDate);
        updatedData.put("quizTimeCategory", updatedQuizTimeCategory);

        quizRef.updateChildren(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UpdateQuizPage.this, "Quiz updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UpdateQuizPage.this, AdminPage.class);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(UpdateQuizPage.this, "Failed to update quiz: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String calculateQuizTimeCategory(Date startDate, Date endDate) {
        Date currentDate = Calendar.getInstance().getTime();

        if (endDate.before(currentDate)) {
            return "previous";
        }

        if (startDate.after(currentDate)) {
            return "upcoming";
        }

        return "ongoing";
    }

    private void setupDifficultyDropdown() {
        String[] difficulties = new String[]{"easy", "medium", "hard"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, difficulties);
        autoCompleteDifficulty.setAdapter(adapter);
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
                    Toast.makeText(UpdateQuizPage.this, "Failed to fetch categories!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                Toast.makeText(UpdateQuizPage.this, "Error fetching categories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateCategoryDropdown(List<Category> categories) {
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        autoCompleteCategory.setAdapter(adapter);

        categoryMap = new HashMap<>();
        for (Category category : categories) {
            categoryMap.put(category.getName(), category.getId());
        }
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
}
