package com.example.quizappassignment2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminPage extends AppCompatActivity {

    private Button returnButton, signOutButton, addQuizButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_page);

        returnButton = findViewById(R.id.admin_btn_return);
        signOutButton = findViewById(R.id.admin_btn_sign_out);
        addQuizButton = findViewById(R.id.admincreatequizbtn);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the previous activity
                onBackPressed();
            }
        });

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out the current user
                FirebaseAuth.getInstance().signOut();
                // Redirect to the login page
                startActivity(new Intent(AdminPage.this, LoginPage.class));
                // Finish the current activity to prevent going back to it using the back button
                finish();
            }
        });

        // Linking the addQuizButton to the CreateQuizPage
        addQuizButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminPage.this, CreateQuizPage.class));
            }
        });
    }
}
