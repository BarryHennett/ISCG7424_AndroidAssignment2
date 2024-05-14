package com.example.exercise1;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Scanner;

public class User {
    public static void main(String[] args) {
        // Initialize Firebase
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setDatabaseUrl("https://fir-exercise1-b0d42-default-rtdb.firebaseio.com/")
                .build();


        // Get a reference to the Firebase Realtime Database
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        // Scanner for user input
        Scanner scanner = new Scanner(System.in);

        // Get user input
        System.out.print("Enter user ID: ");
        String userId = scanner.nextLine();

        System.out.print("Enter name: ");
        String name = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter age: ");
        int age = scanner.nextInt();

        // Save user data
        saveUser(database, userId, name, email, password, age);
    }


    private static void saveUser(DatabaseReference ref, String userId, String name, String email, String password, int age) {
        DatabaseReference usersRef = ref.child("users").child(userId);
        usersRef.child("name").setValue(name);
        usersRef.child("email").setValue(email);
        usersRef.child("password").setValue(password);
        usersRef.child("age").setValue(age);

        System.out.println("User " + userId + " saved successfully.");
    }
}
