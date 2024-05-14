package com.example.exercise1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPassword, editTextAge;
    private Button buttonSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get references to UI elements
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextAge = findViewById(R.id.editTextAge);
        buttonSave = findViewById(R.id.buttonSave);

        // Set the save button click listener
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString();
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                String ageText = editTextAge.getText().toString();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || ageText.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                int age = Integer.parseInt(ageText);

                // Get a reference to the Firebase Realtime Database
                DatabaseReference database = FirebaseDatabase.getInstance().getReference();

                // Save user data
                saveUser(database, name, email, password, age);
            }
        });
    }

    private void saveUser(DatabaseReference ref, String name, String email, String password, int age) {
        DatabaseReference usersRef = ref.child("users").push(); // Automatically generates a unique key
        usersRef.child("name").setValue(name);
        usersRef.child("email").setValue(email);
        usersRef.child("password").setValue(password);
        usersRef.child("age").setValue(age);

        String userId = usersRef.getKey(); // Get the unique key
        Toast.makeText(MainActivity.this, "User " + userId + " saved successfully.", Toast.LENGTH_SHORT).show();
    }
}
