package com.example.collpasingtest.models;

import com.example.collpasingtest.models.PathInfo;

import java.io.Serializable;
import java.util.ArrayList;

public class Path implements PathType, Serializable{

    private int pathType;
    private int payment;
    private int busTransitCount;
    private int busStationCount;
    private int subTransitCount;
    private int subStationCount;
    private int totalTime;
    private int totalWalk;
    private int totalDistance;

    private String firstStationName;
    private String endStationName;
    private ArrayList<PathInfo> pathInfoList;

    public Path() {
    }

    public Path(int pathType, int payment, int busTransitCount, int busStationCount, int subTransitCount, int subStationCount, int totalTime, int totalWalk, int totalDistance,
                String firstStationName, String endStationName, ArrayList<PathInfo> pathInfoList) {
        this.pathType = pathType;
        this.payment = payment;
        this.busTransitCount = busTransitCount;
        this.busStationCount = busStationCount;
        this.subTransitCount = subTransitCount;
        this.subStationCount = subStationCount;
        this.totalTime = totalTime;
        this.totalWalk = totalWalk;
        this.totalDistance = totalDistance;
        this.firstStationName = firstStationName;
        this.endStationName = endStationName;
        this.pathInfoList = pathInfoList;
    }

    public int getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(int totalDistance) {
        this.totalDistance = totalDistance;
    }

    public int getTotalWalk() {
        return totalWalk;
    }

    public void setTotalWalk(int totalWalk) {
        this.totalWalk = totalWalk;
    }

    public int getPathType() {
        return pathType;
    }

    public void setPathType(int pathType) {
        this.pathType = pathType;
    }

    public int getPayment() {
        return payment;
    }

    public void setPayment(int payment) {
        this.payment = payment;
    }

    public int getBusTransitCount() {
        return busTransitCount;
    }

    public void setBusTransitCount(int busTransitCount) {
        this.busTransitCount = busTransitCount;
    }

    public int getBusStationCount() {
        return busStationCount;
    }

    public void setBusStationCount(int busStationCount) {
        this.busStationCount = busStationCount;
    }

    public int getSubTransitCount() {
        return subTransitCount;
    }

    public void setSubTransitCount(int subTransitCount) {
        this.subTransitCount = subTransitCount;
    }

    public int getSubStationCount() {
        return subStationCount;
    }

    public void setSubStationCount(int subStationCount) {
        this.subStationCount = subStationCount;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public String getFirstStationName() {
        return firstStationName;
    }

    public void setFirstStationName(String firstStationName) {
        this.firstStationName = firstStationName;
    }

    public String getEndStationName() {
        return endStationName;
    }

    public void setEndStationName(String endStationName) {
        this.endStationName = endStationName;
    }

    public ArrayList<PathInfo> getPathInfoList() {
        return pathInfoList;
    }

    public void setPathInfoList(ArrayList<PathInfo> pathInfoList) {
        this.pathInfoList = pathInfoList;
    }
}

