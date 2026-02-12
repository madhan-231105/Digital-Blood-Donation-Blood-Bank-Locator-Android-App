package com.example.bloodlink.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bloodlink.R;
import com.example.bloodlink.adapters.DonorAdapter;
import com.example.bloodlink.models.User;
import com.example.bloodlink.utils.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DonorAdapter adapter;
    private List<User> donorList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private DatabaseHelper dbHelper;

    private TextView tvName, tvBloodGroup, tvPhone, tvUserDistrict;
    private ImageView imgProfile, btnEdit, btnLogout, btnNotifications;
    private Button btnOpenMap, btnRequest;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // 🔐 Safety Check
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        dbHelper = new DatabaseHelper(this);

        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigation();

        loadUserProfile();
    }

    // ================= INIT VIEWS =================

    private void initViews() {

        tvName = findViewById(R.id.tvUserName);
        tvBloodGroup = findViewById(R.id.tvUserBloodGroup);
        tvPhone = findViewById(R.id.tvUserPhone);
        tvUserDistrict = findViewById(R.id.tvUserDistrict);

        imgProfile = findViewById(R.id.imgUserProfile);
        btnEdit = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        btnNotifications = findViewById(R.id.btnNotifications);

        btnOpenMap = findViewById(R.id.btnOpenMap);
        btnRequest = findViewById(R.id.btnRequest);

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this::loadDonors);
    }

    // ================= RECYCLER VIEW =================

    private void setupRecyclerView() {

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        donorList = new ArrayList<>();
        adapter = new DonorAdapter(this, donorList);
        recyclerView.setAdapter(adapter);
    }

    // ================= CLICK LISTENERS =================

    private void setupClickListeners() {

        btnEdit.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnNotifications.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationActivity.class)));

        btnOpenMap.setOnClickListener(v ->
                startActivity(new Intent(this, NearbyMapActivity.class)));

        btnRequest.setOnClickListener(v ->
                startActivity(new Intent(this, RequestBloodActivity.class)));
    }

    // ================= BOTTOM NAVIGATION =================

    private void setupBottomNavigation() {

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            }

            if (id == R.id.nav_banks) {
                startActivity(new Intent(this, NearbyMapActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            if (id == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }

    // ================= LOAD USER PROFILE =================

    private void loadUserProfile() {

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) return;

                    User user = snapshot.toObject(User.class);

                    if (user != null) {

                        tvName.setText(user.getName());
                        tvBloodGroup.setText("Blood Group: " + user.getBloodGroup());
                        tvPhone.setText(user.getPhone());
                        tvUserDistrict.setText("District: " + user.getDistrict());

                        loadProfileImage();  // 🔥 Refresh image
                        loadDonors();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load profile",
                                Toast.LENGTH_SHORT).show());
    }

    // ================= AUTO REFRESH WHEN RETURNING =================

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();   // 🔥 reload EVERYTHING
    }

    // ================= LOAD PROFILE IMAGE =================

    private void loadProfileImage() {

        String uid = mAuth.getCurrentUser().getUid();

        Bitmap bmp = dbHelper.getImage(uid);

        if (bmp != null) {
            imgProfile.setImageBitmap(bmp);
        } else {
            imgProfile.setImageResource(R.mipmap.ic_launcher_round);
        }
    }

    // ================= LOAD DONORS =================

    private void loadDonors() {

        swipeRefreshLayout.setRefreshing(true);

        db.collection("users")
                .whereEqualTo("type", "donor")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    donorList.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {

                        User donor = doc.toObject(User.class);

                        if (donor != null &&
                                !donor.getUid().equals(mAuth.getCurrentUser().getUid())) {

                            donorList.add(donor);
                        }
                    }

                    Collections.shuffle(donorList);
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(this,
                            "Failed to load donors",
                            Toast.LENGTH_SHORT).show();
                });
    }
}
