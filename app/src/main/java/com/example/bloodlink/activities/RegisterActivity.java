package com.example.bloodlink.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.bloodlink.R;
import com.example.bloodlink.models.User;
import com.example.bloodlink.utils.DatabaseHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etPhone;
    private Spinner spinnerBloodGroup, spinnerDistrict;
    private ImageView imgProfile;
    private Uri imageUri;
    private Bitmap imageBitmap;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DatabaseHelper dbHelper;

    private FusedLocationProviderClient fusedLocationClient;

    private double latitude = 0.0;
    private double longitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dbHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        spinnerBloodGroup = findViewById(R.id.spinnerBloodGroup);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        imgProfile = findViewById(R.id.imgProfile);
        Button btnRegister = findViewById(R.id.btnRegister);

        requestLocationPermission();
        setupImagePicker();
        btnRegister.setOnClickListener(v -> registerUser());
    }

    // ===============================
    // LOCATION
    // ===============================
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        } else {
            getUserLocation();
        }
    }

    private void getUserLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                });
    }

    // ===============================
    // IMAGE PICKER
    // ===============================
    private void setupImagePicker() {

        ActivityResultLauncher<String> getContent =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {
                            if (uri != null) {
                                imageUri = uri;
                                imgProfile.setImageURI(uri);
                                try {
                                    imageBitmap = MediaStore.Images.Media.getBitmap(
                                            this.getContentResolver(), uri);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

        imgProfile.setOnClickListener(v -> getContent.launch("image/*"));
    }

    // ===============================
    // REGISTER USER
    // ===============================
    private void registerUser() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String bloodGroup = spinnerBloodGroup.getSelectedItem().toString();
        String district = spinnerDistrict.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(phone)) {

            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String uid = authResult.getUser().getUid();

                    // Save profile image in SQLite
                    if (imageBitmap != null) {
                        dbHelper.insertOrUpdateImage(uid, imageBitmap);
                    }

                    saveUserData(uid, name, email, phone,
                            bloodGroup, district);

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    // ===============================
    // SAVE DATA TO FIRESTORE
    // ===============================
    private void saveUserData(String uid,
                              String name,
                              String email,
                              String phone,
                              String bloodGroup,
                              String district) {

        User user = new User(
                uid,
                name,
                email,
                phone,
                bloodGroup,
                district,
                "donor",
                latitude,
                longitude
        );

        db.collection("users")
                .document(uid)
                .set(user)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "Registration Successful",
                            Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(this,
                            MainActivity.class));
                    finish();
                });
    }
}
