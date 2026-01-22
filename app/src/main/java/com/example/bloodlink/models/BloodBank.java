package com.example.bloodlink.models;
import java.util.Map;

public class BloodBank {
    private String name, address, phone;
    private double latitude, longitude;
    private Map<String, Integer> bloodStock; // e.g., "A+": 10

    public BloodBank() {}

    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public Map<String, Integer> getBloodStock() { return bloodStock; }
}