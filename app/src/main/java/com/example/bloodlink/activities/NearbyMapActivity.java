package com.example.bloodlink.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bloodlink.R;
import com.example.bloodlink.adapters.PlaceAdapter;
import com.example.bloodlink.models.Hospital;
import com.example.bloodlink.models.PlaceItem;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class NearbyMapActivity extends AppCompatActivity {

    private static final int LOCATION_REQ = 101;

    private MapView map;
    private RecyclerView list;
    private SearchView searchView;

    private PlaceAdapter adapter;
    private List<PlaceItem> placeList = new ArrayList<>();
    private List<PlaceItem> fullList = new ArrayList<>(); // for search

    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private Location myLocation;

    private Polyline currentRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_nearby_map);

        map = findViewById(R.id.map);
        list = findViewById(R.id.list);
        searchView = findViewById(R.id.searchView);

        map.setMultiTouchControls(true);

        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlaceAdapter(this, placeList);
        list.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPlaces(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPlaces(newText);
                return true;
            }
        });

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQ);
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                myLocation = location;
                showMyLocation(location);
                loadHospitals();
            }
        });
    }

    private void showMyLocation(Location location) {
        GeoPoint me = new GeoPoint(location.getLatitude(), location.getLongitude());
        map.getController().setZoom(13.0);
        map.getController().setCenter(me);

        Marker myMarker = new Marker(map);
        myMarker.setPosition(me);
        myMarker.setTitle("You are here");
        map.getOverlays().add(myMarker);
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        float[] r = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, r);
        return r[0] / 1000;
    }

    private void loadHospitals() {
        db.collection("hospitals")
                .whereEqualTo("city", "Coimbatore")
                .get()
                .addOnSuccessListener(query -> {

                    placeList.clear();
                    fullList.clear();
                    map.getOverlays().clear();
                    showMyLocation(myLocation);

                    android.location.Geocoder geocoder =
                            new android.location.Geocoder(this, Locale.getDefault());

                    for (DocumentSnapshot doc : query) {
                        Hospital h = doc.toObject(Hospital.class);

                        try {
                            List<android.location.Address> addresses =
                                    geocoder.getFromLocationName(h.name + ", Coimbatore", 1);

                            if (addresses != null && !addresses.isEmpty()) {
                                android.location.Address a = addresses.get(0);

                                double lat = a.getLatitude();
                                double lon = a.getLongitude();

                                double d = distance(
                                        myLocation.getLatitude(),
                                        myLocation.getLongitude(),
                                        lat, lon);

                                GeoPoint hospitalPoint = new GeoPoint(lat, lon);

                                Marker m = new Marker(map);
                                m.setPosition(hospitalPoint);
                                m.setTitle(h.name);
                                m.setSubDescription(h.type + " • " + String.format("%.2f km", d));

                                if ("Blood Bank".equals(h.type)) {
                                    m.setIcon(getResources().getDrawable(R.drawable.bloodbank));
                                } else {
                                    m.setIcon(getResources().getDrawable(R.drawable.hospital));
                                }

                                m.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                                    @Override
                                    public boolean onMarkerClick(Marker marker, MapView mapView) {
                                        GeoPoint myPoint = new GeoPoint(
                                                myLocation.getLatitude(),
                                                myLocation.getLongitude()
                                        );
                                        drawRoute(myPoint, hospitalPoint);
                                        return true;
                                    }
                                });

                                map.getOverlays().add(m);

                                PlaceItem item = new PlaceItem(h.name, h.type, d, lat, lon);
                                placeList.add(item);
                                fullList.add(item);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    Collections.sort(placeList,
                            (a, b) -> Double.compare(a.distance, b.distance));

                    adapter.notifyDataSetChanged();
                    map.invalidate();
                });
    }

    // 🔍 SEARCH FILTER
    private void filterPlaces(String text) {
        placeList.clear();
        map.getOverlays().clear();
        showMyLocation(myLocation);

        for (PlaceItem p : fullList) {
            if (p.name.toLowerCase().contains(text.toLowerCase())) {
                placeList.add(p);

                GeoPoint gp = new GeoPoint(p.latitude, p.longitude);
                Marker m = new Marker(map);
                m.setPosition(gp);
                m.setTitle(p.name);
                map.getOverlays().add(m);
            }
        }

        adapter.notifyDataSetChanged();
        map.invalidate();
    }

    // 🚗 Draw route inside app
    private void drawRoute(GeoPoint start, GeoPoint end) {
        new Thread(() -> {
            RoadManager roadManager = new OSRMRoadManager(this, "BloodLink");
            ArrayList<GeoPoint> waypoints = new ArrayList<>();
            waypoints.add(start);
            waypoints.add(end);

            Road road = roadManager.getRoad(waypoints);

            runOnUiThread(() -> {
                if (currentRoute != null) {
                    map.getOverlays().remove(currentRoute);
                }

                currentRoute = RoadManager.buildRoadOverlay(road);
                map.getOverlays().add(currentRoute);
                map.invalidate();
            });
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQ &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
    }
}
