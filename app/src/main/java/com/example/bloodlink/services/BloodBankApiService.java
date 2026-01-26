package com.example.bloodlink.services;

import okhttp3.*;

public class BloodBankApiService {

    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";

    private static String buildQuery(String filter, int radius, double lat, double lng) {
        return "[out:json][timeout:25];\n" +
                "node[" + filter + "](around:" + radius + "," + lat + "," + lng + ");\n" +
                "out body;";
    }

    public static void getNearbyBloodBanksOSM(double lat, double lng, Callback callback) {
        int radius = 5000;
        String query = buildQuery("\"healthcare\"=\"blood_bank\"", radius, lat, lng);
        send(query, callback);
    }

    public static void getNearbyHospitalsOSM(double lat, double lng, Callback callback) {
        int radius = 5000;
        String query = buildQuery("\"amenity\"=\"hospital\"", radius, lat, lng);
        send(query, callback);
    }

    private static void send(String query, Callback callback) {
        RequestBody body = RequestBody.create(query, MediaType.parse("text/plain; charset=utf-8"));

        Request request = new Request.Builder()
                .url(OVERPASS_URL)
                .post(body)
                .build();

        new OkHttpClient().newCall(request).enqueue(callback);
    }
}
