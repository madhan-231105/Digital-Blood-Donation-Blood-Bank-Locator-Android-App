package com.example.bloodlink.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bloodlink.R;
import com.example.bloodlink.adapters.DonorAdapter;
import com.example.bloodlink.models.User;
import com.example.bloodlink.services.EmergencyNotificationService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DonorAdapter adapter;
    private List<User> donorList;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start Emergency Service
        startService(new Intent(this, EmergencyNotificationService.class));

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        donorList = new ArrayList<>();
        adapter = new DonorAdapter(this, donorList);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnOpenMap).setOnClickListener(v -> startActivity(new Intent(this, NearbyMapActivity.class)));
        findViewById(R.id.btnRequest).setOnClickListener(v -> startActivity(new Intent(this, RequestBloodActivity.class)));

        getCurrentLocationAndFetchDonors();
    }

    private void getCurrentLocationAndFetchDonors() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                fetchDonors(location);
            }
        });
    }

    private void fetchDonors(Location myLocation) {
        db.collection("users").whereEqualTo("type", "donor")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    donorList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        // Simple distance filter (e.g., within 50km)
                        float[] results = new float[1];
                        Location.distanceBetween(myLocation.getLatitude(), myLocation.getLongitude(),
                                user.getLatitude(), user.getLongitude(), results);

                        if (results[0] < 50000) { // 50km
                            donorList.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}