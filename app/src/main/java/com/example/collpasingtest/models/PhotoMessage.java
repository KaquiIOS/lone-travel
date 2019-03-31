package com.example.collpasingtest.models;

public class PhotoMessage extends Message{

    private String photoUrl;

    public PhotoMessage() {
        super();
    }

    public PhotoMessage(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
