package com.example.bloodlink.models;

public class User {

    private String uid;
    private String name;
    private String email;
    private String phone;
    private String bloodGroup;
    private String district;
    private String type; // donor or requester
    private double latitude;
    private double longitude;
    private double distance; // for sorting

    public User() {}

    public User(String uid, String name, String email, String phone,
                String bloodGroup, String district,
                String type, double latitude, double longitude) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
        this.district = district;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters & Setters

    public String getUid() { return uid; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getBloodGroup() { return bloodGroup; }
    public String getDistrict() { return district; }
    public String getType() { return type; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getDistance() { return distance; }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
