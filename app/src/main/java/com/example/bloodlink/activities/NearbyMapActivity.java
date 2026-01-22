package com.example.bloodlink.activities;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import com.example.bloodlink.R;
import com.example.bloodlink.models.BloodBank;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class NearbyMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_map);
        db = FirebaseFirestore.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Fetch Blood Banks
        db.collection("blood_banks").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                BloodBank bank = doc.toObject(BloodBank.class);
                LatLng loc = new LatLng(bank.getLatitude(), bank.getLongitude());

                // Show stock in snippet
                String stockInfo = "A+: " + bank.getBloodStock().get("A+");

                mMap.addMarker(new MarkerOptions()
                        .position(loc)
                        .title(bank.getName())
                        .snippet(stockInfo)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }

            // Move camera to a default location (e.g., first bank or user location)
            if (!queryDocumentSnapshots.isEmpty()) {
                BloodBank first = queryDocumentSnapshots.getDocuments().get(0).toObject(BloodBank.class);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(first.getLatitude(), first.getLongitude()), 12));
            }
        });
    }
}