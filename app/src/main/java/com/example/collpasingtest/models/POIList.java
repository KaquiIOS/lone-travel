package com.example.collpasingtest.models;

import java.util.ArrayList;

public class POIList {

    private ArrayList<POI> poi;

    public POIList() {
    }

    public POIList(ArrayList<POI> arrayList) {
        this.poi = (ArrayList<POI>)arrayList.clone();
    }

    public ArrayList<POI> getPois() {
        return poi;
    }

    public void setPois(ArrayList<POI> poi) {
        this.poi = poi;
    }
}
