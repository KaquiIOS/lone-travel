package com.example.collpasingtest.models;

import java.io.Serializable;

public class PassStop implements Serializable{
    private int index;
    private int stationID;
    private double x;
    private double y;
    private String stationName;

    public PassStop() {
    }

    public PassStop(int index, int stationID, double x, double y, String stationName) {
        this.index = index;
        this.stationID = stationID;
        this.x = x;
        this.y = y;
        this.stationName = stationName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getStationID() {
        return stationID;
    }

    public void setStationID(int stationID) {
        this.stationID = stationID;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }
}
