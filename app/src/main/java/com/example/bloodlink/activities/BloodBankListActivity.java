package com.example.bloodlink.activities;

import android.os.Bundle;
import android.util.Log; // Import for logging
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodlink.R;
import com.example.bloodlink.adapters.BloodBankAdapter;
import com.example.bloodlink.models.BloodBank;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BloodBankListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BloodBankAdapter adapter;
    private List<BloodBank> list;
    private ProgressBar progressBar; // Recommended to show loading state

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userDistrict;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_bank_list);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Ensure you have a ProgressBar in your XML, or remove these lines

        recyclerView = findViewById(R.id.recyclerBloodBanks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new BloodBankAdapter(this, list);
        recyclerView.setAdapter(adapter);

        if (mAuth.getCurrentUser() != null) {
            loadUserDistrict();
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserDistrict() {
        if(progressBar != null) progressBar.setVisibility(View.VISIBLE);

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        userDistrict = snapshot.getString("district");

                        Log.d("BloodLink", "User District Found: " + userDistrict);

                        if (userDistrict != null && !userDistrict.isEmpty()) {
                            loadBloodBanks(userDistrict.trim());
                        } else {
                            Toast.makeText(this, "Your profile has no district set", Toast.LENGTH_SHORT).show();
                            if(progressBar != null) progressBar.setVisibility(View.GONE);
                        }
                    } else {
                        Log.e("BloodLink", "User document does not exist");
                        if(progressBar != null) progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("BloodLink", "Error getting user: " + e.getMessage());
                    if(progressBar != null) progressBar.setVisibility(View.GONE);
                });
    }

    private void loadBloodBanks(String targetDistrict) {
        // Query: Type is 'admin' AND District matches
        db.collection("users")
                .whereEqualTo("type", "admin")
                .whereEqualTo("district", targetDistrict)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    list.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("BloodLink", "No blood banks found in " + targetDistrict);
                        Toast.makeText(this, "No Blood Banks found in " + targetDistrict, Toast.LENGTH_SHORT).show();
                    }

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        // We use the exact field names from your Screenshot 1
                        BloodBank bank = new BloodBank();
                        bank.setId(doc.getId());
                        bank.setName(doc.getString("bankName")); // Matches screenshot
                        bank.setType(doc.getString("bankType")); // Matches screenshot
                        bank.setDistrict(doc.getString("district"));
                        bank.setPhone(doc.getString("phone"));

                        list.add(bank);
                    }
                    adapter.notifyDataSetChanged();
                    if(progressBar != null) progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    // 🔥 THIS IS WHERE YOU CHECK FOR INDEX ERRORS
                    Log.e("BloodLink", "Error loading banks: " + e.getMessage());
                    Toast.makeText(BloodBankListActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    if(progressBar != null) progressBar.setVisibility(View.GONE);
                });
    }
}