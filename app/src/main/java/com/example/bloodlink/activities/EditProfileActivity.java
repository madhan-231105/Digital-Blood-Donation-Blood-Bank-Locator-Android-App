package com.example.bloodlink.activities;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bloodlink.R;
import com.example.bloodlink.utils.DatabaseHelper; // Import Helper
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etPhone;
    private Spinner spinnerBloodGroup;
    private ImageView imgProfile;
    private Button btnSave;
    private Bitmap imageBitmap;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dbHelper = new DatabaseHelper(this);

        etName = findViewById(R.id.etEditName);
        etPhone = findViewById(R.id.etEditPhone);
        spinnerBloodGroup = findViewById(R.id.spinnerEditBloodGroup);
        imgProfile = findViewById(R.id.imgEditProfile);
        btnSave = findViewById(R.id.btnSaveChanges);

        loadUserData();

        ActivityResultLauncher<String> getContent = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if(uri != null) {
                        imgProfile.setImageURI(uri);
                        try {
                            imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (IOException e) { e.printStackTrace(); }
                    }
                });

        imgProfile.setOnClickListener(v -> getContent.launch("image/*"));
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void loadUserData() {
        String uid = mAuth.getCurrentUser().getUid();

        // 1. Load Text from Firebase
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                etName.setText(documentSnapshot.getString("name"));
                etPhone.setText(documentSnapshot.getString("phone"));
                // (Set Spinner logic here as before...)
            }
        });

        // 2. Load Image from SQLite
        Bitmap bmp = dbHelper.getImage(uid);
        if (bmp != null) {
            imgProfile.setImageBitmap(bmp);
        }
    }

    private void saveChanges() {
        String uid = mAuth.getCurrentUser().getUid();

        // 1. Update SQLite Image
        if (imageBitmap != null) {
            dbHelper.insertOrUpdateImage(uid, imageBitmap);
        }

        // 2. Update Firebase Text Data
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", etName.getText().toString());
        updates.put("phone", etPhone.getText().toString());
        updates.put("bloodGroup", spinnerBloodGroup.getSelectedItem().toString());

        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}