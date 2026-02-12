package com.example.bloodlink.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bloodlink.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class BloodBankDetailActivity extends AppCompatActivity {

    private TextView tvName, tvType, tvPhone, tvDistrict, tvStock;
    private Button btnCall;

    private FirebaseFirestore db;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_bank_detail);

        tvName = findViewById(R.id.tvDetailName);
        tvType = findViewById(R.id.tvDetailType);
        tvPhone = findViewById(R.id.tvDetailPhone);
        tvDistrict = findViewById(R.id.tvDetailDistrict);
        tvStock = findViewById(R.id.tvDetailStock);
        btnCall = findViewById(R.id.btnCallBank);

        db = FirebaseFirestore.getInstance();

        // ✅ Get bankId only once
        String bankId = getIntent().getStringExtra("bankId");

        if (bankId != null) {
            loadBankDetails(bankId);
            loadStock(bankId);
        }

        btnCall.setOnClickListener(v -> {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
            }
        });
    }

    // ✅ Load basic details from users collection
    private void loadBankDetails(String bankId) {

        db.collection("users")
                .document(bankId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.exists()) {

                        tvName.setText(snapshot.getString("bankName"));
                        tvType.setText("Type: " + snapshot.getString("bankType"));
                        tvDistrict.setText("District: " + snapshot.getString("district"));

                        phoneNumber = snapshot.getString("phone");
                        tvPhone.setText("Phone: " + phoneNumber);
                    }
                });
    }

    // ✅ Load stock from blood_banks collection
    private void loadStock(String bankId) {

        db.collection("blood_banks")
                .document(bankId)
                .collection("stock")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {
                        tvStock.setText("Available Stock: No stock data");
                        return;
                    }

                    StringBuilder stockText = new StringBuilder("Available Stock:\n");

                    for (var doc : querySnapshot.getDocuments()) {

                        String bloodType = doc.getString("bloodType");
                        Long units = doc.getLong("units");

                        if (bloodType != null && units != null) {
                            stockText.append(bloodType)
                                    .append(" : ")
                                    .append(units)
                                    .append(" units\n");
                        }
                    }

                    tvStock.setText(stockText.toString());
                })
                .addOnFailureListener(e ->
                        tvStock.setText("Available Stock: Error loading data"));
    }
}
