package com.example.bloodlink.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap; // Import added
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodlink.R;
import com.example.bloodlink.adapters.DonorAdapter;
import com.example.bloodlink.models.User;
import com.example.bloodlink.services.EmergencyNotificationService;
import com.example.bloodlink.utils.DatabaseHelper; // Import added
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Existing Variables
    private RecyclerView recyclerView;
    private DonorAdapter adapter;
    private List<User> donorList;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;

    // Profile Card Variables
    private TextView tvName, tvBloodGroup, tvPhone;
    private ImageView imgProfile, btnEdit;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize Firebase Instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 2. Start Emergency Service
        startService(new Intent(this, EmergencyNotificationService.class));

        // 3. Initialize Profile Card Views
        tvName = findViewById(R.id.tvUserName);
        tvBloodGroup = findViewById(R.id.tvUserBloodGroup);
        tvPhone = findViewById(R.id.tvUserPhone);
        imgProfile = findViewById(R.id.imgUserProfile);
        btnEdit = findViewById(R.id.btnEditProfile);

        // 4. Initialize RecyclerView for Donors
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        donorList = new ArrayList<>();
        adapter = new DonorAdapter(this, donorList);
        recyclerView.setAdapter(adapter);

        // 5. Button Listeners
        findViewById(R.id.btnOpenMap).setOnClickListener(v -> startActivity(new Intent(this, NearbyMapActivity.class)));
        findViewById(R.id.btnRequest).setOnClickListener(v -> startActivity(new Intent(this, RequestBloodActivity.class)));

        // Handle Edit Profile Click
        btnEdit.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, EditProfileActivity.class));
        });

        // 6. Load Data
        loadUserProfile(); // Load own profile
        getCurrentLocationAndFetchDonors(); // Load nearby donors
    }

    /**
     * Refresh profile data when returning from EditProfileActivity
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    /**
     * Loads text from Firebase and Image from SQLite
     */
    private void loadUserProfile() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        // 1. Load Text Data (Name, Phone, Blood Group) from Firestore
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    tvName.setText(user.getName());
                    tvBloodGroup.setText("Blood Group: " + user.getBloodGroup());
                    tvPhone.setText(user.getPhone());
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(MainActivity.this, "Failed to load profile info", Toast.LENGTH_SHORT).show();
        });

        // 2. Load Profile Image from Local SQLite Database
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        Bitmap bmp = dbHelper.getImage(uid);

        if (bmp != null) {
            imgProfile.setImageBitmap(bmp);
        } else {
            // Default image if no image found in SQLite
            imgProfile.setImageResource(R.mipmap.ic_launcher_round);
        }
    }

    /**
     * Gets GPS location and filters donors
     */
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
                        // Exclude current user from donor list
                        if (user != null && !user.getUid().equals(mAuth.getCurrentUser().getUid())) {

                            // Calculate Distance
                            float[] results = new float[1];
                            Location.distanceBetween(myLocation.getLatitude(), myLocation.getLongitude(),
                                    user.getLatitude(), user.getLongitude(), results);

                            if (results[0] < 50000) { // Show donors within 50km
                                donorList.add(user);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}