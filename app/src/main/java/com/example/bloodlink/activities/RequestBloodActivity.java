package com.example.bloodlink.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodlink.R;
import com.example.bloodlink.models.User;   // ✅ IMPORTANT
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

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etPhone = findViewById(R.id.etReqPhone);
        etLocationMsg = findViewById(R.id.etReqLocation);
        spinnerBloodGroup = findViewById(R.id.spinnerReqBloodGroup);
        Button btnSubmit = findViewById(R.id.btnSubmitRequest);

        loadUserPhone();

        btnSubmit.setOnClickListener(v -> submitRequest());
    }

    // ==========================================
    // LOAD LOGGED-IN USER PHONE
    // ==========================================

    private void loadUserPhone() {

        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String phone = documentSnapshot.getString("phone");

                        if (!TextUtils.isEmpty(phone)) {
                            etPhone.setText(phone);
                            etPhone.setEnabled(false);
                        }
                    }
                });
    }

    // ==========================================
    // SUBMIT BLOOD REQUEST
    // ==========================================

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
                TextUtils.isEmpty(locationMsg) ||
                bloodGroup.equals("Select Blood Group")) {

            Toast.makeText(this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        // Fetch user to get district
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        Toast.makeText(this,
                                "User data not found",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    User user = snapshot.toObject(User.class);

                    if (user == null ||
                            TextUtils.isEmpty(user.getDistrict())) {

                        Toast.makeText(this,
                                "District not set in profile",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> request = new HashMap<>();
                    request.put("requesterUid", uid);
                    request.put("requesterName", user.getName());   // ✅ ADD THIS
                    request.put("phone", phone);
                    request.put("locationMessage", locationMsg);
                    request.put("bloodGroup", bloodGroup);
                    request.put("district", user.getDistrict());   // ✅ FIXED
                    request.put("status", "pending");
                    request.put("timestamp", System.currentTimeMillis());

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
                                            "Failed: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error fetching user data",
                                Toast.LENGTH_SHORT).show());
    }
}
