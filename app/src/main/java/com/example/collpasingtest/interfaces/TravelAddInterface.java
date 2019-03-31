package com.example.collpasingtest.interfaces;

import android.net.Uri;

import com.example.collpasingtest.models.Route;

import java.util.ArrayList;
import java.util.Date;

public interface TravelAddInterface {
    void setCity(int cityNum);
    void setDateInfo(Date startDate, Date endDate);
    void setContent(String content, String photoPath);
}
