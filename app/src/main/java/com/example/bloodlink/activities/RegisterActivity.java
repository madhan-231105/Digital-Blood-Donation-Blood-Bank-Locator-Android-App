package com.example.bloodlink.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bloodlink.R;
import com.example.bloodlink.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etPhone, etBloodGroup;
    private ImageView imgProfile;
    private Uri imageUri;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etBloodGroup = findViewById(R.id.etBloodGroup);
        imgProfile = findViewById(R.id.imgProfile);
        Button btnRegister = findViewById(R.id.btnRegister);

        // Image Picker
        ActivityResultLauncher<String> getContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    imageUri = uri;
                    imgProfile.setImageURI(uri);
                });

        imgProfile.setOnClickListener(v -> getContent.launch("image/*"));

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (imageUri != null) {
                        uploadImageAndSaveData(authResult.getUser().getUid());
                    } else {
                        saveUserData(authResult.getUser().getUid(), null);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void uploadImageAndSaveData(String uid) {
        StorageReference fileRef = storageRef.child(uid + ".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> saveUserData(uid, uri.toString()))
        );
    }

    private void saveUserData(String uid, String imageUrl) {
        // Hardcoded location for demo. In real app, get current GPS location here.
        double dummyLat = 28.7041;
        double dummyLng = 77.1025;

        User user = new User(uid, etName.getText().toString(), etEmail.getText().toString(),
                etPhone.getText().toString(), etBloodGroup.getText().toString(), "donor", dummyLat, dummyLng);

        if(imageUrl != null) user.setProfileImageUrl(imageUrl);

        db.collection("users").document(uid).set(user)
                .addOnSuccessListener(v -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                });
    }
}