package com.example.collpasingtest.models;

public class POISearchInfo {
    private String totalCount;
    private String count;
    private String page;
    private POIList pois;

    public POISearchInfo() {
    }

    public POISearchInfo(String totalCount, String count, String page, POIList pois) {
        this.totalCount = totalCount;
        this.count = count;
        this.page = page;
        this.pois = pois;
    }

    public String getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(String totalCount) {
        this.totalCount = totalCount;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public POIList getPois() {
        return pois;
    }

    public void setPois(POIList pois) {
        this.pois = pois;
    }
}
