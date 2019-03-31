package com.example.collpasingtest.adapters;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.example.collpasingtest.models.SearchInfo;
import com.example.collpasingtest.views.LocationInfoFragment;

import java.util.ArrayList;

public class LocationItemAdapter extends FragmentStatePagerAdapter {

    private static final int MAX_CARD_NUM = 20;

    private Fragment[] fragments;
    private int count;

    public LocationItemAdapter(FragmentManager fm) {
        super(fm);
        count = 0;
        fragments = new Fragment[MAX_CARD_NUM];
        for(int i = 0; i < MAX_CARD_NUM; ++i)
            fragments[i] = new LocationInfoFragment();
    }

    public void setLocationInfo(SearchInfo info) {
        if(count == 20) return;

        Bundle bundle = new Bundle();
        bundle.putSerializable("info", info);
        fragments[count++].setArguments(bundle);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return count;
    }

    public void clear() {
        count = 0;
    }
}
