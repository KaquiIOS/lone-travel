package com.example.collpasingtest.models;

import com.google.firebase.auth.FirebaseUser;

public class User {

    private String uid;
    private String email;
    private String name;
    private String profileUrl;

    public User() {
    }

    public User(String uid, String email, String name, String profileUrl) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.profileUrl = profileUrl;
    }

    public User(FirebaseUser user) {
        this.uid = user.getUid();
        this.email = user.getEmail();
        this.name = user.getDisplayName();
        // photoUrl 이 항상 value 가 아니라서 예외처리 필수
        if(user.getPhotoUrl() != null)
            this.profileUrl = user.getPhotoUrl().toString();
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getProfileUrl() {
        return profileUrl;
    }
}
