package com.example.quizappassignment2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, signUpButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.editTextemail);
        TextInputLayout passwordLayout = findViewById(R.id.loginpasswordtxt);
        EditText passwordEditText = passwordLayout.getEditText();
        loginButton = findViewById(R.id.loginbtn);
        signUpButton = findViewById(R.id.signupbtnlgn);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    signIn(email, password);
                } else {
                    Toast.makeText(LoginPage.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                    signUp(email, password);
                } else {
                    Toast.makeText(LoginPage.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Check user role and redirect accordingly
                            checkUserRole(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginPage.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            checkUserRole(user);
                        } else {
                            // If sign up fails, display a message to the user.
                            Toast.makeText(LoginPage.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void checkUserRole(FirebaseUser user) {
        // Check if the user is signed in (not null)
        if (user != null) {
            // Check user's email or any other attribute to determine if they are an admin
            // For demonstration, let's assume admin@example.com is an admin
            String adminEmail = "admin@email.com";
            if (user.getEmail().equals(adminEmail)) {
                // Admin user, redirect to admin page
                startActivity(new Intent(LoginPage.this, AdminPage.class));
            } else {
                // Regular user, redirect to user page
                startActivity(new Intent(LoginPage.this, UserPage.class));
            }
            // Finish the login activity to prevent going back to it using the back button
            finish();
        }
    }
}