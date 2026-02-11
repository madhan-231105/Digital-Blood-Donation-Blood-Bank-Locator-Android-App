package com.example.bloodlink.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodlink.R;
import com.example.bloodlink.models.User;
import com.example.bloodlink.utils.DatabaseHelper;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etPhone;
    private AutoCompleteTextView autoBloodGroup, autoDistrict;
    private ImageView imgProfile;
    private Bitmap imageBitmap;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DatabaseHelper dbHelper;

    private final String[] bloodGroups = {
            "A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"
    };

    private final String[] districts = {
            "Chennai", "Coimbatore", "Madurai", "Salem", "Trichy"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        dbHelper = new DatabaseHelper(this);

        etName = findViewById(R.id.etEditName);
        etPhone = findViewById(R.id.etEditPhone);
        autoBloodGroup = findViewById(R.id.autoBloodGroup);
        autoDistrict = findViewById(R.id.autoDistrict);
        imgProfile = findViewById(R.id.imgEditProfile);
        Button btnSave = findViewById(R.id.btnSaveChanges);

        setupDropdowns();
        loadUserData();
        setupImagePicker();

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void setupDropdowns() {

        ArrayAdapter<String> bloodAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line,
                        bloodGroups);

        ArrayAdapter<String> districtAdapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line,
                        districts);

        autoBloodGroup.setAdapter(bloodAdapter);
        autoDistrict.setAdapter(districtAdapter);
    }

    private void loadUserData() {

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    User user = documentSnapshot.toObject(User.class);

                    if (user != null) {
                        etName.setText(user.getName());
                        etPhone.setText(user.getPhone());

                        // 🔥 Show DB value in dropdown
                        autoBloodGroup.setText(user.getBloodGroup(), false);
                        autoDistrict.setText(user.getDistrict(), false);
                    }
                });

        Bitmap bmp = dbHelper.getImage(uid);
        if (bmp != null) {
            imgProfile.setImageBitmap(bmp);
        }
    }

    private void setupImagePicker() {

        ActivityResultLauncher<String> getContent =
                registerForActivityResult(
                        new ActivityResultContracts.GetContent(),
                        uri -> {
                            if (uri != null) {
                                try {
                                    imageBitmap = MediaStore.Images
                                            .Media
                                            .getBitmap(getContentResolver(), uri);
                                    imgProfile.setImageBitmap(imageBitmap);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

        imgProfile.setOnClickListener(v -> getContent.launch("image/*"));
    }

    private void saveChanges() {

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String bloodGroup = autoBloodGroup.getText().toString().trim();
        String district = autoDistrict.getText().toString().trim();

        if (TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(bloodGroup) ||
                TextUtils.isEmpty(district)) {

            Toast.makeText(this,
                    "Fill all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .update(
                        "name", name,
                        "phone", phone,
                        "bloodGroup", bloodGroup,
                        "district", district
                )
                .addOnSuccessListener(unused -> {

                    if (imageBitmap != null) {
                        dbHelper.insertOrUpdateImage(uid, imageBitmap);
                    }

                    Toast.makeText(this,
                            "Profile Updated",
                            Toast.LENGTH_SHORT).show();

                    finish();
                });
    }
}
