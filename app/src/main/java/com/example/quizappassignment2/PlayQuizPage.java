package com.example.quizappassignment2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

public class PlayQuizPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_quiz_page);

        ViewPager viewPager = findViewById(R.id.viewPager);
        List<Fragment> fragments = new ArrayList<>();
        // Add your question fragments here
        fragments.add(new QuestionFragment());
        fragments.add(new QuestionFragment());
        // Add more fragments as per the number of questions

        QuestionPagerAdapter adapter = new QuestionPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
    }

    public static class QuestionFragment extends Fragment {
        // Here you can define your QuestionFragment class
        // Include the necessary methods and logic for handling each question fragment
    }

    private static class QuestionPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments;

        public QuestionPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
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
    }
}
