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
        setContentView(R.layout.activity_request_blood); // Make sure to create this XML

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etPhone = findViewById(R.id.etReqPhone);
        etLocationMsg = findViewById(R.id.etReqLocation);
        spinnerBloodGroup = findViewById(R.id.spinnerReqBloodGroup);
        Button btnSubmit = findViewById(R.id.btnSubmitRequest);

        btnSubmit.setOnClickListener(v -> submitRequest());
    }

    private void submitRequest() {
        String phone = etPhone.getText().toString().trim();
        String locationMsg = etLocationMsg.getText().toString().trim();
        String bloodGroup = spinnerBloodGroup.getSelectedItem().toString();

        if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(locationMsg)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> request = new HashMap<>();
        request.put("requesterUid", mAuth.getCurrentUser().getUid());
        request.put("phone", phone);
        request.put("locationMessage", locationMsg);
        request.put("bloodGroup", bloodGroup);
        request.put("timestamp", System.currentTimeMillis());

        db.collection("blood_requests").add(request)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Emergency Request Sent!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to send request", Toast.LENGTH_SHORT).show());
    }
}