package com.example.collpasingtest.models;

import java.util.ArrayList;
import java.util.Date;

public class SharedRouteInfo {

    private String sharingUserID;
    private String title;
    private String content;
    private String routeKey;
    private String selectedCity;
    private ArrayList<String> tags;
    private ArrayList<SearchInfo> routeList;
    private Date enrollDate;
    private int likeNum, sharedNum;

    public SharedRouteInfo() {
    }

    public SharedRouteInfo(String sharingUserID, String title, String content, String routeKey,
                           String selectedCity, ArrayList<String> tags,  ArrayList<SearchInfo> routeList, Date enrollDate, int likeNum, int sharedNum) {
        this.sharingUserID = sharingUserID;
        this.title = title;
        this.content = content;
        this.routeKey = routeKey;
        this.selectedCity = selectedCity;
        this.tags = tags;
        this.routeList = routeList;
        this.enrollDate = enrollDate;
        this.likeNum = likeNum;
        this.sharedNum = sharedNum;
    }

    public int getSharedNum() {
        return sharedNum;
    }

    public void setSharedNum(int sharedNum) {
        this.sharedNum = sharedNum;
    }

    public String getSharingUserID() {
        return sharingUserID;
    }

    public void setSharingUserID(String sharingUserID) {
        this.sharingUserID = sharingUserID;
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

    public String getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(String routeKey) {
        this.routeKey = routeKey;
    }

    public String getSelectedCity() {
        return selectedCity;
    }

    public void setSelectedCity(String selectedCity) {
        this.selectedCity = selectedCity;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }

    public ArrayList<SearchInfo> getRouteList() {
        return routeList;
    }

    public void setRouteList(ArrayList<SearchInfo> routeList) {
        this.routeList = routeList;
    }

    public Date getEnrollDate() {
        return enrollDate;
    }

    public void setEnrollDate(Date enrollDate) {
        this.enrollDate = enrollDate;
    }

    public int getLikeNum() {
        return likeNum;
    }

    public void setLikeNum(int likeNum) {
        this.likeNum = likeNum;
    }
}
