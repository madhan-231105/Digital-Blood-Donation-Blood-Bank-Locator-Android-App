package com.example.bloodlink.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bloodlink.R;
import com.example.bloodlink.models.User;
import com.example.bloodlink.utils.DatabaseHelper; // Import Helper
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etPhone;
    private Spinner spinnerBloodGroup;
    private ImageView imgProfile;
    private Uri imageUri;
    private Bitmap imageBitmap; // To store selected image
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DatabaseHelper dbHelper; // SQLite Helper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dbHelper = new DatabaseHelper(this); // Init SQLite

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        spinnerBloodGroup = findViewById(R.id.spinnerBloodGroup);
        imgProfile = findViewById(R.id.imgProfile);
        Button btnRegister = findViewById(R.id.btnRegister);

        // Image Picker
        ActivityResultLauncher<String> getContent = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        imgProfile.setImageURI(uri);
                        try {
                            // Convert URI to Bitmap for SQLite
                            imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        imgProfile.setOnClickListener(v -> getContent.launch("image/*"));
        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String bloodGroup = spinnerBloodGroup.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    // 1. Save Image to SQLite
                    if (imageBitmap != null) {
                        dbHelper.insertOrUpdateImage(uid, imageBitmap);
                    }

                    // 2. Save Text Data to Firebase (No image URL needed now)
                    saveUserData(uid);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveUserData(String uid) {
        User user = new User(uid, etName.getText().toString(), etEmail.getText().toString(),
                etPhone.getText().toString(), spinnerBloodGroup.getSelectedItem().toString(),
                "donor", 28.7041, 77.1025);

        // We don't set profileImageUrl anymore because it's in SQLite
        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(v -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }
}