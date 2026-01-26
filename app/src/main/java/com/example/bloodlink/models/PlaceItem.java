package com.example.bloodlink.models;

public class PlaceItem {
    public String name;
    public String type;
    public double distance;
    public double latitude;
    public double longitude;
    public boolean isFavorite;

    public PlaceItem(String name, String type, double distance,
                     double latitude, double longitude) {
        this.name = name;
        this.type = type;
        this.distance = distance;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isFavorite = false;
    }
}
