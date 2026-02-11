package com.example.bloodlink.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
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

public class AdminRegisterActivity extends AppCompatActivity {

    private EditText etBankName, etEmail, etPhone, etPassword;
    private Spinner spinnerDistrict, spinnerBankType;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etBankName = findViewById(R.id.etBankName);
        etEmail = findViewById(R.id.etAdminEmail);
        etPhone = findViewById(R.id.etAdminPhone);
        etPassword = findViewById(R.id.etAdminPassword);

        spinnerDistrict = findViewById(R.id.spinnerAdminDistrict);
        spinnerBankType = findViewById(R.id.spinnerBankType);

        setupBankTypeSpinner();

        Button btnRegister = findViewById(R.id.btnAdminRegister);
        btnRegister.setOnClickListener(v -> registerAdmin());
    }

    private void setupBankTypeSpinner() {

        String[] types = {
                "Select Blood Bank Type",
                "With Hospital",
                "Private",
                "Government"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_dropdown_item,
                        types);

        spinnerBankType.setAdapter(adapter);
    }

    private void registerAdmin() {

        String bankName = etBankName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String district = spinnerDistrict.getSelectedItem().toString();
        String bankType = spinnerBankType.getSelectedItem().toString();

        if (TextUtils.isEmpty(bankName) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(password) ||
                district.equals("Select District") ||
                bankType.equals("Select Blood Bank Type")) {

            Toast.makeText(this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    String uid = mAuth.getCurrentUser().getUid();

                    Map<String, Object> admin = new HashMap<>();
                    admin.put("uid", uid);
                    admin.put("bankName", bankName);
                    admin.put("email", email);
                    admin.put("phone", phone);
                    admin.put("district", district);
                    admin.put("bankType", bankType);
                    admin.put("type", "admin");
                    admin.put("createdAt", System.currentTimeMillis());

                    db.collection("users")
                            .document(uid)
                            .set(admin)
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(this,
                                        "Admin Registered Successfully",
                                        Toast.LENGTH_LONG).show();

                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this,
                                            "Firestore Error: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Registration Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }
}
