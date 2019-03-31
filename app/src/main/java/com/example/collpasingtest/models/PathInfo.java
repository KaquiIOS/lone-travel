package com.example.collpasingtest.models;

import java.io.Serializable;
import java.util.ArrayList;

public class PathInfo implements Serializable{
    private int trafficType;
    private int distance;
    private int sectionTime;

    private String vehicleName;

    private ArrayList<PassStop> passStopList;

    public PathInfo() {
    }

    public PathInfo(int trafficType, int distance, int sectionTime) {
        this.trafficType = trafficType;
        this.distance = distance;
        this.sectionTime = sectionTime;
    }

    public PathInfo(int trafficType, int distance, int sectionTime, String vehicleName, ArrayList<PassStop> passStopList) {
        this.trafficType = trafficType;
        this.distance = distance;
        this.vehicleName = vehicleName;
        this.sectionTime = sectionTime;
        this.passStopList = passStopList;
    }

    public int getTrafficType() {
        return trafficType;
    }

    public void setTrafficType(int trafficType) {
        this.trafficType = trafficType;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getSectionTime() {
        return sectionTime;
    }

    public void setSectionTime(int sectionTime) {
        this.sectionTime = sectionTime;
    }

    public ArrayList<PassStop> getPassStopList() {
        return passStopList;
    }

    public void setPassStopList(ArrayList<PassStop> passStopList) {
        this.passStopList = passStopList;
    }
}
