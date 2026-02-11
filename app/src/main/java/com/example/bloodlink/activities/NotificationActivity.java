package com.example.bloodlink.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodlink.R;
import com.example.bloodlink.adapters.NotificationAdapter;
import com.example.bloodlink.models.BloodRequest;
import com.example.bloodlink.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<BloodRequest> requestList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String myBloodGroup;
    private String myDistrict;

    private ListenerRegistration requestListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        requestList = new ArrayList<>();
        adapter = new NotificationAdapter(this, requestList);
        recyclerView.setAdapter(adapter);

        loadUserAndFetchRequests();
    }

    private void loadUserAndFetchRequests() {

        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    User user = snapshot.toObject(User.class);

                    if (user != null) {

                        if (!"donor".equals(user.getType())) {
                            Toast.makeText(this,
                                    "Only donors receive requests",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        myBloodGroup = user.getBloodGroup();
                        myDistrict = user.getDistrict();

                        fetchMatchingRequests();
                    }
                });
    }

    private void fetchMatchingRequests() {

        String myUid = mAuth.getCurrentUser().getUid();

        requestListener = db.collection("blood_requests")
                .whereEqualTo("bloodGroup", myBloodGroup)
                .whereEqualTo("district", myDistrict)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((value, error) -> {

                    if (error != null) {
                        Toast.makeText(this,
                                "Error loading requests",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    requestList.clear();

                    if (value != null) {

                        for (DocumentSnapshot doc : value.getDocuments()) {

                            BloodRequest request =
                                    doc.toObject(BloodRequest.class);

                            if (request != null) {

                                // 🔥 VERY IMPORTANT LINE
                                request.setId(doc.getId());

                                // Hide if rejected by this user
                                List<String> rejectedList =
                                        (List<String>) doc.get("rejectedBy");

                                if (rejectedList != null &&
                                        rejectedList.contains(myUid)) {
                                    continue;
                                }

                                requestList.add(request);
                            }
                        }

                        // Sort newest first
                        Collections.sort(requestList,
                                (r1, r2) ->
                                        Long.compare(r2.getTimestamp(),
                                                r1.getTimestamp()));

                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestListener != null) {
            requestListener.remove();
        }
    }
}
