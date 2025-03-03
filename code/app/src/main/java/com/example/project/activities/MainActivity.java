package com.example.project.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // new methods
        checkUserLoginStatus();
    }

    private void checkUserLoginStatus() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            // login->MoodHistoryActivity
            startActivity(new Intent(this, MoodHistoryActivity.class));
        } else {
            // !login-> LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}