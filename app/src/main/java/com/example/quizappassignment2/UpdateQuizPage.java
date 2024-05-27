package com.example.quizappassignment2;

import android.content.Intent;
import android.os.Bundle;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Date;
import java.text.SimpleDateFormat;
public class UpdateQuizPage extends AppCompatActivity implements DateRangePickerDialog.OnDateRangeSelectedListener {

    private EditText editTextnamecreatequiz;
    private AutoCompleteTextView autoCompleteCategory, autoCompleteDifficulty;
    private Button btnSelectStart, btnSelectEnd, updatequizbtn, cancelcreatequiz;

    private DatabaseReference quizzesRef;
    private String quizId;
    private Date selectedStartDate, selectedEndDate;

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

        // Get the quiz ID from the intent
        quizId = getIntent().getStringExtra("quizId");
        if (quizId == null) {
            Toast.makeText(this, "Invalid quiz ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get current user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get a reference to the "quizzes" node in Firebase Realtime Database
        quizzesRef = FirebaseDatabase.getInstance().getReference().child("quizzes");

        // Query the database to find the quiz associated with the given quizId
        quizzesRef.child(quizId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve quiz data
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String category = dataSnapshot.child("category").getValue(String.class);
                    String difficulty = dataSnapshot.child("difficulty").getValue(String.class);

                    // Set data to fields
                    editTextnamecreatequiz.setText(name);
                    autoCompleteCategory.setText(category);
                    autoCompleteDifficulty.setText(difficulty);

                } else {
                    // Quiz not found
                    Toast.makeText(UpdateQuizPage.this, "Quiz not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
                Toast.makeText(UpdateQuizPage.this, "Error loading quiz: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Set click listener for the start date button
        btnSelectStart.setOnClickListener(v -> launchDateRangePickerDialog(true));

        // Set click listener for the end date button
        btnSelectEnd.setOnClickListener(v -> launchDateRangePickerDialog(false));

        // Set click listener for the update button
        updatequizbtn.setOnClickListener(v -> updateQuiz());

        // Set click listener for the cancel button
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
        // Get updated data from fields
        String updatedName = editTextnamecreatequiz.getText().toString();
        String updatedCategory = autoCompleteCategory.getText().toString();
        String updatedDifficulty = autoCompleteDifficulty.getText().toString();

        // Get the reference to the quiz node in the Firebase Realtime Database
        DatabaseReference quizRef = FirebaseDatabase.getInstance().getReference().child("quizzes").child(quizId);

        // Create a HashMap to hold the updated data
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", updatedName);
        updatedData.put("category", updatedCategory);
        updatedData.put("difficulty", updatedDifficulty);

        // Format start date as DD/MM/YYYY
        if (selectedStartDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedStartDate = dateFormat.format(selectedStartDate);
            updatedData.put("startDate", formattedStartDate);
        }

        // Format end date as DD/MM/YYYY
        if (selectedEndDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedEndDate = dateFormat.format(selectedEndDate);
            updatedData.put("endDate", formattedEndDate);
        }

        // Recalculate quizTimeCategory based on the new dates
        String updatedQuizTimeCategory = calculateQuizTimeCategory(selectedStartDate, selectedEndDate);

        // Add updated quizTimeCategory to the HashMap
        updatedData.put("quizTimeCategory", updatedQuizTimeCategory);

        // Perform the update operation
        quizRef.updateChildren(updatedData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Quiz updated successfully
                        Toast.makeText(UpdateQuizPage.this, "Quiz updated successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UpdateQuizPage.this, AdminPage.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update quiz
                        Toast.makeText(UpdateQuizPage.this, "Failed to update quiz: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
}