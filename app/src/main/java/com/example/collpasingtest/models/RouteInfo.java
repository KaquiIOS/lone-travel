package com.example.collpasingtest.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RouteInfo implements Serializable{


    private ArrayList<PathType> routeList = new ArrayList<>();
    private String title;
    private String content;
    private String routeID;
    private boolean isShared = false;

    public RouteInfo() {
    }

    public RouteInfo(ArrayList<PathType> routeList, String title, String content, String routeID, boolean isShared) {
        this.routeList = routeList;
        this.title = title;
        this.content = content;
        this.routeID = routeID;
        this.isShared = isShared;
    }

    public boolean isShared() {
        return isShared;
    }

    public void setShared(boolean shared) {
        isShared = shared;
    }

    public String getRouteID() {
        return routeID;
    }

    public void setRouteID(String routeID) {
        this.routeID = routeID;
    }

    public ArrayList<PathType> getRouteList() {
        return routeList;
    }

    public void setRouteList(ArrayList<PathType> routeList) {
        this.routeList = routeList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
