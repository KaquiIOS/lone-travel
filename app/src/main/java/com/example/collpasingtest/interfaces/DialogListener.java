package com.example.collpasingtest.interfaces;

import com.example.collpasingtest.models.SearchInfo;

import java.util.ArrayList;

public interface DialogListener {
    public void enrollInfo(String title, String content);
    public void cancel();
}
