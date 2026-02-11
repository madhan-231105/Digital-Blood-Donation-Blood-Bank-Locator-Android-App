package com.example.bloodlink.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bloodlink.R;
import com.example.bloodlink.adapters.StockAdapter;
import com.example.bloodlink.models.Stock;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView tvBankName, tvBankType, tvBankPhone, tvBankDistrict;
    private Spinner spinnerBloodType;
    private EditText etStockUnits;
    private Button btnUpdateStock;
    private SwipeRefreshLayout swipeRefresh;

    private RecyclerView recyclerStock;
    private StockAdapter stockAdapter;
    private List<Stock> stockList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // ================= FIREBASE INIT =================
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // ================= TOP BAR BUTTONS =================
        ImageView btnLogout = findViewById(R.id.btnAdminLogout);
        ImageView btnNotifications = findViewById(R.id.btnAdminNotifications);

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, WelcomeActivity.class));
            finish();
        });

        btnNotifications.setOnClickListener(v ->
                startActivity(new Intent(this, DonorListActivity.class))
        );


        // ================= BIND VIEWS =================
        tvBankName = findViewById(R.id.tvBankName);
        tvBankType = findViewById(R.id.tvBankType);
        tvBankPhone = findViewById(R.id.tvBankPhone);
        tvBankDistrict = findViewById(R.id.tvBankDistrict);

        spinnerBloodType = findViewById(R.id.spinnerBloodType);
        etStockUnits = findViewById(R.id.etStockUnits);
        btnUpdateStock = findViewById(R.id.btnUpdateStock);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        // ================= RECYCLER SETUP =================
        recyclerStock = findViewById(R.id.recyclerStock);
        recyclerStock.setLayoutManager(new LinearLayoutManager(this));

        stockList = new ArrayList<>();
        stockAdapter = new StockAdapter(stockList);
        recyclerStock.setAdapter(stockAdapter);

        // ================= LOAD DATA =================
        loadAdminProfile();
        loadStock();

        btnUpdateStock.setOnClickListener(v -> updateStock());

        // ================= SWIPE REFRESH =================
        swipeRefresh.setOnRefreshListener(() -> {
            loadAdminProfile();
            loadStock();
        });
    }

    // ================= LOAD ADMIN PROFILE =================

    private void loadAdminProfile() {

        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.exists()) {

                        tvBankName.setText(
                                snapshot.getString("bankName") != null
                                        ? snapshot.getString("bankName")
                                        : "Unknown Bank");

                        tvBankType.setText("Type: " +
                                snapshot.getString("bankType"));

                        tvBankPhone.setText("Phone: " +
                                snapshot.getString("phone"));

                        tvBankDistrict.setText("District: " +
                                snapshot.getString("district"));
                    }

                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Failed to load profile",
                            Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                });
    }

    // ================= LOAD STOCK =================

    private void loadStock() {

        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("blood_banks")
                .document(uid)
                .collection("stock")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    stockList.clear();

                    for (var doc : querySnapshot.getDocuments()) {

                        Stock stock = doc.toObject(Stock.class);
                        if (stock != null) {
                            stockList.add(stock);
                        }
                    }

                    stockAdapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Failed to load stock",
                            Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                });
    }

    // ================= UPDATE STOCK =================

    private void updateStock() {

        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();
        String bloodType = spinnerBloodType.getSelectedItem().toString();
        String unitsStr = etStockUnits.getText().toString().trim();

        if (TextUtils.isEmpty(unitsStr)) {
            Toast.makeText(this,
                    "Enter units",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int units;

        try {
            units = Integer.parseInt(unitsStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this,
                    "Invalid number",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> stockData = new HashMap<>();
        stockData.put("bloodType", bloodType);
        stockData.put("units", units);
        stockData.put("updatedAt", System.currentTimeMillis());

        db.collection("blood_banks")
                .document(uid)
                .collection("stock")
                .document(bloodType)
                .set(stockData)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(this,
                            "Stock Updated Successfully",
                            Toast.LENGTH_SHORT).show();

                    etStockUnits.setText("");
                    loadStock();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }
}
