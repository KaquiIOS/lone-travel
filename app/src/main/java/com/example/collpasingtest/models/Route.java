package com.example.collpasingtest.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;

public class Route implements Serializable{

    private double latitude;
    private double longitude;
    private String region;
    private String city;
    private String dong;

    public Route(double latitude, double longitude, String region, String city, String dong) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.region = region;
        this.city = city;
        this.dong = dong;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDong() {
        return dong;
    }

    public void setDong(String dong) {
        this.dong = dong;
    }
}
