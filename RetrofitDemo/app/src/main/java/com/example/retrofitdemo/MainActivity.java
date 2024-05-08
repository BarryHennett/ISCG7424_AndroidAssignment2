package com.example.retrofitdemo;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    UserControlerRESTAPI restapi;
    Users users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void LoadUsers(View view) {
        restapi = new UserControlerRESTAPI();
        restapi.start();
    }

    public void DisplayUsers(View view) {
        TextView tv = findViewById(R.id.tv);
        if(restapi !=null){
            users = restapi.getUsers();
            if (users !=null){
                tv.setText("Users Count : "+ users.data.size()+"\n"+
                        users.data.toString());
                tv.setMovementMethod(new ScrollingMovementMethod());
            }
            else {
                tv.setText("Empty List");
            }
        }else {
            tv.setText("Users Not Loaded");
        }
    }
}