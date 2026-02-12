package com.example.bloodlink.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodlink.R;
import com.example.bloodlink.adapters.RequestHistoryAdapter;
import com.example.bloodlink.models.RequestModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RequestHistoryAdapter adapter;
    private List<RequestModel> requestList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerHistory);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestList = new ArrayList<>();
        adapter = new RequestHistoryAdapter(this, requestList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadMyRequests();
    }

    private void loadMyRequests() {

        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("blood_requests")   // ✅ correct collection
                .whereEqualTo("requesterUid", uid)   // ✅ correct field
                .get()
                .addOnSuccessListener(query -> {

                    requestList.clear();

                    for (var doc : query.getDocuments()) {

                        RequestModel model = doc.toObject(RequestModel.class);

                        if (model != null) {
                            model.setId(doc.getId());
                            requestList.add(model);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (requestList.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load history",
                                Toast.LENGTH_SHORT).show());
    }
}
