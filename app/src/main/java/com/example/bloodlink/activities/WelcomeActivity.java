package com.example.bloodlink.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodlink.R;
import com.google.android.material.button.MaterialButton;

public class WelcomeActivity extends AppCompatActivity {

    private MaterialButton btnUser, btnAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        btnUser = findViewById(R.id.btnUserLogin);
        btnAdmin = findViewById(R.id.btnAdminLogin);

        // ================= USER LOGIN =================
        btnUser.setOnClickListener(v -> {

            Intent intent = new Intent(
                    WelcomeActivity.this,
                    LoginActivity.class   // User login
            );

            startActivity(intent);
        });

        // ================= ADMIN LOGIN =================
        btnAdmin.setOnClickListener(v -> {

            Intent intent = new Intent(
                    WelcomeActivity.this,
                    AdminLoginActivity.class   // Separate admin login
            );

            startActivity(intent);
        });
    }
}
