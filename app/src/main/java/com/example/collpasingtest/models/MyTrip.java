package com.example.collpasingtest.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyTrip implements Serializable{

    public static final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

    private String tripKey;
    private String region;
    private Date startDate;
    private Date endDate;

    public MyTrip() {
    }

    public MyTrip(String tripKey, String region, Date startDate, Date endDate) {
        this.tripKey = tripKey;
        this.region = region;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getParsedEndDate() {
        return sf.format(endDate);
    }

    public String getParsedStartDate() {
        return sf.format(startDate);
    }


    public String getTripKey() {
        return tripKey;
    }

    public void setTripKey(String tripKey) {
        this.tripKey = tripKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
