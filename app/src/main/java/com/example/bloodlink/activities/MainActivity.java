package com.example.bloodlink.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bloodlink.R;
import com.example.bloodlink.adapters.DonorAdapter;
import com.example.bloodlink.models.User;
import com.example.bloodlink.services.EmergencyNotificationService;
import com.example.bloodlink.utils.DatabaseHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 101;

    private RecyclerView recyclerView;
    private DonorAdapter adapter;
    private List<User> donorList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient fusedLocationClient;

    private TextView tvName, tvBloodGroup, tvPhone, tvUserDistrict;
    private ImageView imgProfile, btnEdit, btnLogout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private double currentLatitude = 0;
    private double currentLongitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Start emergency background service
        startService(new Intent(this, EmergencyNotificationService.class));

        // Bind UI
        tvName = findViewById(R.id.tvUserName);
        tvBloodGroup = findViewById(R.id.tvUserBloodGroup);
        tvPhone = findViewById(R.id.tvUserPhone);
        tvUserDistrict = findViewById(R.id.tvUserDistrict);
        imgProfile = findViewById(R.id.imgUserProfile);
        btnEdit = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);

        // RecyclerView setup
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        donorList = new ArrayList<>();
        adapter = new DonorAdapter(this, donorList);
        recyclerView.setAdapter(adapter);

        // Swipe refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            checkLocationPermission();
        });

        // Buttons
        findViewById(R.id.btnOpenMap)
                .setOnClickListener(v ->
                        startActivity(new Intent(this, NearbyMapActivity.class)));
        findViewById(R.id.btnNotifications)
                .setOnClickListener(v ->
                        startActivity(new Intent(this, NotificationActivity.class)));
        findViewById(R.id.btnRequest)
                .setOnClickListener(v ->
                        startActivity(new Intent(this, RequestBloodActivity.class)));

        btnEdit.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        loadUserProfile();
        checkLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    // ================= PROFILE =================

    private void loadUserProfile() {

        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    User user = documentSnapshot.toObject(User.class);

                    if (user != null) {
                        tvName.setText(user.getName());
                        tvBloodGroup.setText("Blood Group: " + user.getBloodGroup());
                        tvPhone.setText(user.getPhone());
                        tvUserDistrict.setText("District: " + user.getDistrict());
                    }
                });

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Bitmap bmp = dbHelper.getImage(uid);

        if (bmp != null) {
            imgProfile.setImageBitmap(bmp);
        } else {
            imgProfile.setImageResource(R.mipmap.ic_launcher_round);
        }
    }

    // ================= LOCATION =================

    private void checkLocationPermission() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);

        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            getCurrentLocation();

        } else {
            Toast.makeText(this,
                    "Location permission required",
                    Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location != null) {

                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();

                        updateUserLocationInFirestore();
                        fetchDonors(location);

                    } else {
                        requestFreshLocation();
                    }
                });
    }

    // If last location is null
    private void requestFreshLocation() {

        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMaxUpdates(1)
                .build();

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null)
                .addOnSuccessListener(location -> {

                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();

                        updateUserLocationInFirestore();
                        fetchDonors(location);
                    }
                });
    }

    private void updateUserLocationInFirestore() {

        if (mAuth.getCurrentUser() == null) return;

        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .update(
                        "latitude", currentLatitude,
                        "longitude", currentLongitude
                );
    }

    // ================= DONOR FETCH =================

    private void fetchDonors(Location myLocation) {

        db.collection("users")
                .whereEqualTo("type", "donor")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    donorList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        User user = doc.toObject(User.class);

                        if (user != null &&
                                !user.getUid().equals(mAuth.getCurrentUser().getUid())) {

                            if (user.getLatitude() == 0 &&
                                    user.getLongitude() == 0) continue;

                            float[] results = new float[1];

                            Location.distanceBetween(
                                    myLocation.getLatitude(),
                                    myLocation.getLongitude(),
                                    user.getLatitude(),
                                    user.getLongitude(),
                                    results
                            );

                            double distanceKm = results[0] / 1000;

                            if (distanceKm < 50) {
                                user.setDistance(distanceKm);
                                donorList.add(user);
                            }
                        }
                    }

                    Collections.sort(donorList,
                            (u1, u2) ->
                                    Double.compare(u1.getDistance(), u2.getDistance()));

                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e ->
                        swipeRefreshLayout.setRefreshing(false));
    }
}
