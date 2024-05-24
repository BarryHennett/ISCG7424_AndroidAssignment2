package com.example.quizappassignment2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.Collections;
import java.util.List;

public class PlayQuizPage extends AppCompatActivity {

    private ViewPager viewPager;
    private Button nextButton;
    private Button returnButton;
    private QuestionPagerAdapter adapter;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private String quizId;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_quiz_page);

        viewPager = findViewById(R.id.viewPager);
        nextButton = findViewById(R.id.nextbtn);
        returnButton = findViewById(R.id.quizplayreturnbtn);

        questions = new ArrayList<>();
        adapter = new QuestionPagerAdapter(getSupportFragmentManager(), new ArrayList<>());
        viewPager.setAdapter(adapter);

        // Retrieve the quiz ID from the intent
        quizId = getIntent().getStringExtra("quizId");
        if (quizId == null) {
            Toast.makeText(this, "Invalid quiz ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchQuestionsFromFirebase();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer();
                if (currentQuestionIndex < questions.size() - 1) {
                    currentQuestionIndex++;
                    viewPager.setCurrentItem(currentQuestionIndex);
                } else {
                    showFinalScore();
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
        DatabaseReference questionsRef = FirebaseDatabase.getInstance().getReference("quizzes").child(quizId).child("questions");
        questionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Question question = snapshot.getValue(Question.class);
                    if (question != null) {
                        questions.add(question);
                    }
                }
                updateViewPager();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PlayQuizPage.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        for (Question question : questions) {
            fragments.add(QuestionFragment.newInstance(question));
        }
        adapter.setFragments(fragments);
        adapter.notifyDataSetChanged();
        viewPager.setAdapter(adapter); // Update the ViewPager's adapter
    }

    private void checkAnswer() {
        QuestionFragment fragment = (QuestionFragment) adapter.getItem(currentQuestionIndex);
        if (fragment != null && fragment.isAnswerSelected()) {
            if (fragment.isAnswerCorrect()) {
                score++;
                Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Incorrect!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void showFinalScore() {
        Toast.makeText(this, "Quiz completed. Your score: " + score + " out of 10", Toast.LENGTH_LONG).show();
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

    public static class QuestionFragment extends Fragment {
        private static final String ARG_QUESTION = "question";
        private Question question;
        private RadioButton option1, option2, option3, option4;
        private String correctAnswer;

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
                correctAnswer = question.getCorrectAnswer();
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_question, container, false);
            TextView questionTextView = view.findViewById(R.id.questiontext);
            option1 = view.findViewById(R.id.option1);
            option2 = view.findViewById(R.id.option2);
            option3 = view.findViewById(R.id.option3);
            option4 = view.findViewById(R.id.option4);

            if (question != null) {
                questionTextView.setText(question.getQuestion());
                List<String> options = new ArrayList<>(question.getIncorrectAnswers());
                options.add(correctAnswer);

                // Shuffle options to randomize their order
                Collections.shuffle(options);

                // Ensure options list has at least four elements
                if (options.size() >= 4) {
                    option1.setText(options.get(0));
                    option2.setText(options.get(1));
                    option3.setText(options.get(2));
                    option4.setText(options.get(3));
                } else {
                    // Handle the case where there are not enough options
                    // This could be logging an error, displaying a message, or any other appropriate action
                    Toast.makeText(getContext(), "Not enough options for question", Toast.LENGTH_SHORT).show();
                }
            }

            return view;
        }


        public boolean isAnswerSelected() {
            return option1.isChecked() || option2.isChecked() || option3.isChecked() || option4.isChecked();
        }

        public boolean isAnswerCorrect() {
            if (option1.isChecked()) {
                return option1.getText().toString().equals(correctAnswer);
            } else if (option2.isChecked()) {
                return option2.getText().toString().equals(correctAnswer);
            } else if (option3.isChecked()) {
                return option3.getText().toString().equals(correctAnswer);
            } else if (option4.isChecked()) {
                return option4.getText().toString().equals(correctAnswer);
            }
            // If no option is selected, return false
            return false;
        }

    }


    public static class Question implements Serializable {
        private String question;
        private List<String> incorrectAnswers;
        private String correctAnswer;

        public Question() {
        }
        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public List<String> getIncorrectAnswers() {
            return incorrectAnswers;
        }

        public void setIncorrectAnswers(List<String> incorrectAnswers) {
            this.incorrectAnswers = incorrectAnswers;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public void setCorrectAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
        }
    }
}
