package com.example.bloodlink.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodlink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RequestBloodActivity extends AppCompatActivity {

    private EditText etPhone, etLocationMsg;
    private Spinner spinnerBloodGroup;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_blood);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Bind Views
        etPhone = findViewById(R.id.etReqPhone);
        etLocationMsg = findViewById(R.id.etReqLocation);
        spinnerBloodGroup = findViewById(R.id.spinnerReqBloodGroup);
        Button btnSubmit = findViewById(R.id.btnSubmitRequest);

        // Auto load logged-in user phone
        loadUserPhone();

        // Submit button
        btnSubmit.setOnClickListener(v -> submitRequest());
    }

    // ==============================
    // FETCH USER PHONE FROM FIRESTORE
    // ==============================

    private void loadUserPhone() {

        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String phone = documentSnapshot.getString("phone");

                        if (phone != null) {
                            etPhone.setText(phone);

                            // Optional: make phone non-editable
                            etPhone.setEnabled(false);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load phone",
                                Toast.LENGTH_SHORT).show());
    }

    // ==============================
    // SUBMIT REQUEST
    // ==============================

    private void submitRequest() {

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this,
                    "User not logged in",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String phone = etPhone.getText().toString().trim();
        String locationMsg = etLocationMsg.getText().toString().trim();
        String bloodGroup = spinnerBloodGroup.getSelectedItem().toString();

        if (TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(locationMsg)) {

            Toast.makeText(this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (bloodGroup.equals("Select Blood Group")) {
            Toast.makeText(this,
                    "Please select blood group",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Create request map
        Map<String, Object> request = new HashMap<>();
        request.put("requesterUid", mAuth.getCurrentUser().getUid());
        request.put("phone", phone);
        request.put("locationMessage", locationMsg);
        request.put("bloodGroup", bloodGroup);
        request.put("timestamp", System.currentTimeMillis());
        request.put("status", "pending");

        db.collection("blood_requests")
                .add(request)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(this,
                            "Emergency Request Sent!",
                            Toast.LENGTH_LONG).show();

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to send request",
                                Toast.LENGTH_SHORT).show());
    }
}
