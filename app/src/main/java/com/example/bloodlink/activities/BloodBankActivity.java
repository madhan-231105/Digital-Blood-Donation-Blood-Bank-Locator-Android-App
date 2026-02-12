package com.example.bloodlink.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodlink.R;
import com.example.bloodlink.adapters.BloodBankAdapter;
import com.example.bloodlink.models.BloodBank;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BloodBankActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BloodBankAdapter adapter;
    private List<BloodBank> bankList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String userDistrict;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blood_bank);

        recyclerView = findViewById(R.id.recyclerViewBanks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bankList = new ArrayList<>();
        adapter = new BloodBankAdapter(this, bankList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadUserDistrict(); // 🔥 Load district first
    }

    private void loadUserDistrict() {

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    userDistrict = snapshot.getString("district");

                    if (userDistrict != null) {
                        userDistrict = userDistrict.trim();
                        loadBloodBanks(); // 🔥 Load banks after district fetched
                    }
                });
    }

    private void loadBloodBanks() {

        if (userDistrict == null) return;

        db.collection("users")
                .whereEqualTo("type", "admin")
                .whereEqualTo("district", userDistrict) // ✅ FILTER HERE
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    bankList.clear();

                    for (var doc : querySnapshot.getDocuments()) {

                        BloodBank bank = new BloodBank();
                        bank.setId(doc.getId());
                        bank.setName(doc.getString("bankName"));
                        bank.setType(doc.getString("bankType"));
                        bank.setDistrict(doc.getString("district"));
                        bank.setPhone(doc.getString("phone"));

                        bankList.add(bank);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load blood banks",
                                Toast.LENGTH_SHORT).show());
    }
}
