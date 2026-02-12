package com.example.bloodlink.models;

import java.util.Map;

public class BloodBank {

    private String id;
    private String name;
    private String type;
    private String district;
    private String phone;
    private Map<String, Integer> bloodStock;

    public BloodBank() {
        // Required for Firestore
    }

    public BloodBank(String id, String name, String type,
                     String district, String phone,
                     Map<String, Integer> bloodStock) {

        this.id = id;
        this.name = name;
        this.type = type;
        this.district = district;
        this.phone = phone;
        this.bloodStock = bloodStock;
    }

    // GETTERS

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDistrict() {
        return district;
    }

    public String getPhone() {
        return phone;
    }

    public Map<String, Integer> getBloodStock() {
        return bloodStock;
    }

    // SETTERS (important for Firestore)

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setBloodStock(Map<String, Integer> bloodStock) {
        this.bloodStock = bloodStock;
    }
}
