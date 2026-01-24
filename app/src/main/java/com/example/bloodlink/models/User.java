package com.example.bloodlink.models;

public class User {
    private String uid, name, email, phone, bloodGroup, profileImageUrl, type;
    private double latitude, longitude;

    public User() {} // Required for Firestore

    public User(String uid, String name, String email, String phone, String bloodGroup, String type, double lat, double lng) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
        this.type = type;
        this.latitude = lat;
        this.longitude = lng;
    }

    // --- ADDED GETTER FOR UID ---
    public String getUid() { return uid; }

    public String getName() { return name; }
    public String getBloodGroup() { return bloodGroup; }
    public String getPhone() { return phone; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}