package com.example.exercise1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPassword, editTextAge;
    private Button buttonSave, buttonUpdate, buttonDelete;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList = new ArrayList<>();
    private DatabaseReference database;
    private String selectedUserId;

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
        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonDelete = findViewById(R.id.buttonDelete);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(userList, new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                editTextName.setText(user.getName());
                editTextEmail.setText(user.getEmail());
                editTextPassword.setText(user.getPassword());
                editTextAge.setText(String.valueOf(user.getAge()));
                selectedUserId = user.getId();
            }
        });
        recyclerView.setAdapter(userAdapter);

        // Get a reference to the Firebase Realtime Database
        database = FirebaseDatabase.getInstance().getReference();

        // Load existing users
        loadUsers();

        // Set the save button click listener
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveOrUpdateUser();
            }
        });

        // Set the update button click listener
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveOrUpdateUser();
            }
        });

        // Set the delete button click listener
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
            }
        });
    }

    private void loadUsers() {
        database.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    user.setId(snapshot.getKey());
                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveOrUpdateUser() {
        String name = editTextName.getText().toString();
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        String ageText = editTextAge.getText().toString();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || ageText.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageText);

        if (selectedUserId == null) {
            // Save new user
            DatabaseReference usersRef = database.child("users").push(); // Automatically generates a unique key
            usersRef.child("name").setValue(name);
            usersRef.child("email").setValue(email);
            usersRef.child("password").setValue(password);
            usersRef.child("age").setValue(age);

            Toast.makeText(MainActivity.this, "User saved successfully.", Toast.LENGTH_SHORT).show();
        } else {
            // Update existing user
            DatabaseReference userRef = database.child("users").child(selectedUserId);
            userRef.child("name").setValue(name);
            userRef.child("email").setValue(email);
            userRef.child("password").setValue(password);
            userRef.child("age").setValue(age);

            Toast.makeText(MainActivity.this, "User updated successfully.", Toast.LENGTH_SHORT).show();
            selectedUserId = null;
        }

        clearFields();
    }

    private void deleteUser() {
        if (selectedUserId == null) {
            Toast.makeText(MainActivity.this, "Please select a user to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = database.child("users").child(selectedUserId);
        userRef.removeValue();

        Toast.makeText(MainActivity.this, "User deleted successfully.", Toast.LENGTH_SHORT).show();
        selectedUserId = null;
        clearFields();
    }

    private void clearFields() {
        editTextName.setText("");
        editTextEmail.setText("");
        editTextPassword.setText("");
        editTextAge.setText("");
    }
}
