package com.example.quizappassignment2;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserPage extends AppCompatActivity implements QuizAdapter.OnItemClickListener {

    private static final String ONGOING = "ongoing";
    private static final String UPCOMING = "upcoming";
    private static final String PARTICIPATED = "participated";
    private static final String PREVIOUS = "previous";

    private Button signOutButton;
    private RecyclerView recyclerView;
    private QuizAdapter quizAdapter;
    private List<DataSnapshot> allQuizzes;
    private Map<String, List<DataSnapshot>> categorizedQuizzes;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        recyclerView = findViewById(R.id.recyclerviewuser);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        allQuizzes = new ArrayList<>();
        categorizedQuizzes = new HashMap<>();
        quizAdapter = new QuizAdapter(this, allQuizzes);
        recyclerView.setAdapter(quizAdapter);
        quizAdapter.setOnItemClickListener(this); // Set item click listener

        tabLayout = findViewById(R.id.user_tab);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tabPosition = tab.getPosition();
                displayQuizzes(tabPosition);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        signOutButton = findViewById(R.id.userbtnsignout);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UserPage.this, LoginPage.class));
                finish();
            }
        });

        fetchQuizzesFromFirebase();
    }

    private void fetchQuizzesFromFirebase() {
        FirebaseDatabase.getInstance("https://iscg7427assignment2-default-rtdb.firebaseio.com/")
                .getReference("quizzes")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        allQuizzes.clear();
                        categorizedQuizzes.clear();
                        for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                            allQuizzes.add(quizSnapshot);
                            categorizeQuiz(quizSnapshot);
                        }
                        Log.d(TAG, "All Quizzes Size: " + allQuizzes.size());
                        for (Map.Entry<String, List<DataSnapshot>> entry : categorizedQuizzes.entrySet()) {
                            Log.d(TAG, "Category: " + entry.getKey() + ", Size: " + entry.getValue().size());
                        }
                        quizAdapter.notifyDataSetChanged();
                        int selectedTabPosition = tabLayout.getSelectedTabPosition();
                        displayQuizzes(selectedTabPosition);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(UserPage.this, "Failed to fetch quizzes", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void categorizeQuiz(DataSnapshot quizSnapshot) {
        String category = quizSnapshot.child("quizTimeCategory").getValue(String.class);
        if (category != null) {
            List<DataSnapshot> quizzes = categorizedQuizzes.get(category);
            if (quizzes == null) {
                quizzes = new ArrayList<>();
                categorizedQuizzes.put(category, quizzes);
            }
            quizzes.add(quizSnapshot);
        }
    }

    private void displayQuizzes(int tabPosition) {
        String category;
        switch (tabPosition) {
            case 0:
                category = ONGOING;
                break;
            case 1:
                category = UPCOMING;
                break;
            case 2:
                category = PARTICIPATED;
                break;
            case 3:
                category = PREVIOUS;
                break;
            default:
                category = ONGOING;
                break;
        }
        Log.d(TAG, "Displaying quizzes for category: " + category);

        List<DataSnapshot> quizzes = categorizedQuizzes.get(category);
        if (quizzes != null) {
            quizAdapter.setData(quizzes);
        } else {
            quizAdapter.setData(new ArrayList<DataSnapshot>());
        }
    }

    @Override
    public void onItemClick(DataSnapshot quizSnapshot) {
        String quizId = quizSnapshot.getKey();
        String category = quizSnapshot.child("quizTimeCategory").getValue(String.class);
        boolean isPlayable = ONGOING.equals(category);
        Intent intent = new Intent(this, QuizDetailPage.class);
        intent.putExtra("quizId", quizId);
        intent.putExtra("isPlayable", isPlayable);
        startActivity(intent);
    }
}
