package com.example.collpasingtest.models;

import java.io.Serializable;

public class SearchInfo implements Serializable, PathType {

    private String title;
    private String address;
    private String telNo;
    private String bizName;

    private double latitude;  // noor
    private double longitude; // noor

    public SearchInfo() {
    }

    public SearchInfo(String title, String address, double latitude, double longitude,
                      String telNo, String bizName) {
        this.title = title;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.telNo = telNo;
        this.bizName = bizName;
    }

    public String getTelNo() {
        return telNo;
    }

    public void setTelNo(String telNo) {
        this.telNo = telNo;
    }

    public String getBizName() {
        return bizName;
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return title + "\n" + address + "\n" +
                bizName + "\n" + telNo + "\n" +
                latitude + "\n" + longitude + "\n";
    }
}
