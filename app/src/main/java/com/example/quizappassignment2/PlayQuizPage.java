package com.example.quizappassignment2;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlayQuizPage extends AppCompatActivity {

    private ViewPager viewPager;
    private Button nextButton;
    private Button returnButton;
    private QuestionPagerAdapter adapter;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private String quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_quiz_page);

        Intent intent = getIntent();
        if (intent != null) {
            quizId = intent.getStringExtra("quizId");
        }

        viewPager = findViewById(R.id.viewPager);
        nextButton = findViewById(R.id.nextbtn);
        returnButton = findViewById(R.id.quizplayreturnbtn);

        questions = new ArrayList<>();
        adapter = new QuestionPagerAdapter(getSupportFragmentManager(), new ArrayList<>());
        viewPager.setAdapter(adapter);

        fetchQuestionsFromFirebase();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentQuestionIndex < questions.size() - 1) {
                    currentQuestionIndex++;
                    viewPager.setCurrentItem(currentQuestionIndex);
                } else {
                    Toast.makeText(PlayQuizPage.this, "You have completed the quiz!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void fetchQuestionsFromFirebase() {
        if (quizId != null) {
            DatabaseReference questionsRef = FirebaseDatabase.getInstance().getReference("quizzes").child(quizId).child("questions");
            questionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Question question = snapshot.getValue(Question.class);
                        questions.add(question);
                    }
                    updateViewPager();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(PlayQuizPage.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "No quiz ID found", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        for (Question question : questions) {
            fragments.add(QuestionFragment.newInstance(question));
        }
        adapter.setFragments(fragments);
        adapter.notifyDataSetChanged();
    }

    public static class QuestionFragment extends Fragment {
        private static final String ARG_QUESTION = "question";

        private Question question;

        public static QuestionFragment newInstance(Question question) {
            QuestionFragment fragment = new QuestionFragment();
            Bundle args = new Bundle();
            args.putSerializable(ARG_QUESTION, question);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                question = (Question) getArguments().getSerializable(ARG_QUESTION);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_question, container, false);
            TextView questionTextView = view.findViewById(R.id.questiontext);
            if (question != null) {
                questionTextView.setText(question.getQuestion());
            }
            return view;
        }
    }

    private static class QuestionPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        public QuestionPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void setFragments(List<Fragment> fragments) {
            this.fragments = fragments;
        }
    }


    public static class Question implements Serializable {
        private String question;
        private List<String> incorrect_answers;
        private String correct_answer;

        // Constructors, getters, and setters
        public Question() {
        }

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
}