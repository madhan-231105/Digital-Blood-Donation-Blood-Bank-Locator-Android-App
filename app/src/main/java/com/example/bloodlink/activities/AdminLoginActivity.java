package com.example.bloodlink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodlink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etAdminEmail);
        etPassword = findViewById(R.id.etAdminPassword);

        findViewById(R.id.btnAdminLogin)
                .setOnClickListener(v -> loginAdmin());

        findViewById(R.id.tvAdminRegister)
                .setOnClickListener(v ->
                        startActivity(new Intent(this,
                                AdminRegisterActivity.class)));
    }

    private void loginAdmin() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String uid = mAuth.getCurrentUser().getUid();

                    db.collection("users").document(uid)
                            .get()
                            .addOnSuccessListener(snapshot -> {

                                String role = snapshot.getString("type");

                                if ("admin".equals(role)) {

                                    Toast.makeText(this,
                                            "Admin Login Successful",
                                            Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(this,
                                            AdminDashboardActivity.class));
                                    finish();

                                } else {

                                    Toast.makeText(this,
                                            "This account is not Admin",
                                            Toast.LENGTH_LONG).show();

                                    mAuth.signOut();
                                }
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Login Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}
