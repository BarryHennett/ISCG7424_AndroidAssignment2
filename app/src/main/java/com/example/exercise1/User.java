package com.example.exercise1;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class User {
    public static void main(String[] args) {
        // Initialize Firebase
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setDatabaseUrl("https://your-database-name.firebaseio.com")
                .build();


        // Get a reference to the Firebase Realtime Database
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        // Save user data
        saveUser(database, "unique_user_id", "John Doe", "john@example.com");
    }

    private static void saveUser(DatabaseReference ref, String userId, String name, String email) {
        DatabaseReference usersRef = ref.child("users").child(userId);
        usersRef.child("name").setValue(name);
        usersRef.child("email").setValue(email);
        System.out.println("User " + userId + " saved successfully.");
    }
}
