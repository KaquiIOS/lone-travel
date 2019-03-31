package com.example.collpasingtest.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Travel implements Serializable{

    private String contentId;
    private String writerUid;
    private Date writeDate;
    private String content;
    private String photoPath;
    private String region;
    private Date travelStartDate;
    private Date travelEndDate;
    private RouteInfo routeInfo;


    public Travel() {
    }

    public Travel(String contentId, String writerUid, Date writeDate, String content, String region, String photoPath,
                  Date travelStartDate, Date travelEndDate, RouteInfo routeInfo) {
        this.contentId = contentId;
        this.writerUid = writerUid;
        this.writeDate = writeDate;
        this.content = content;
        this.region = region;
        this.photoPath = photoPath;
        this.travelStartDate = travelStartDate;
        this.travelEndDate = travelEndDate;
        this.routeInfo = routeInfo;
    }

    public RouteInfo getRouteInfo() {
        return routeInfo;
    }

    public void setRouteInfo(RouteInfo routeInfo) {
        this.routeInfo = routeInfo;
    }

    public String getWriterUid() {
        return writerUid;
    }

    public void setWriterUid(String writerUid) {
        this.writerUid = writerUid;
    }

    public void setWriteDate(Date writeDate) {
        this.writeDate = writeDate;
    }

    public Date getWriteDate() {
        return writeDate;
    }

    public Date getTravelStartDate() {
        return travelStartDate;
    }

    public Date getTravelEndDate() {
        return travelEndDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public void setTravelStartDate(Date travelStartDate) {
        this.travelStartDate = travelStartDate;
    }

    public void setTravelEndDate(Date travelEndDate) {
        this.travelEndDate = travelEndDate;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
