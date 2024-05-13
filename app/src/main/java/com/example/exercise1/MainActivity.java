package com.example.exercise1;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Example of saving user data
        saveUserData("unique_user_id", "John Doe", "john@example.com");
    }

    private void saveUserData(String userId, String name, String email) {
        DatabaseReference usersRef = mDatabase.child("users").child(userId);
        usersRef.child("name").setValue(name);
        usersRef.child("email").setValue(email);
        Log.d("MainActivity", "User " + userId + " saved successfully.");
    }
}
