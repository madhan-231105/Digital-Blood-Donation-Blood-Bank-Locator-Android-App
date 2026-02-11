package com.example.bloodlink.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodlink.R;
import com.example.bloodlink.adapters.DonorAdapter;
import com.example.bloodlink.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class DonorListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DonorAdapter adapter;
    private List<User> donorList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String adminDistrict;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_list);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerDonors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        donorList = new ArrayList<>();

        // ✅ FIXED LINE (Context first)
        adapter = new DonorAdapter(this, donorList);

        recyclerView.setAdapter(adapter);

        loadAdminDistrict();
    }

    private void loadAdminDistrict() {

        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.exists()) {
                        adminDistrict = snapshot.getString("district");
                        loadDonors();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load admin data",
                                Toast.LENGTH_SHORT).show());
    }

    private void loadDonors() {

        if (adminDistrict == null) return;

        db.collection("users")
                .whereEqualTo("type", "donor")
                .whereEqualTo("district", adminDistrict)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    donorList.clear();

                    for (var doc : querySnapshot.getDocuments()) {

                        User user = doc.toObject(User.class);

                        if (user != null) {
                            donorList.add(user);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load donors",
                                Toast.LENGTH_SHORT).show());
    }
}
