package com.example.collpasingtest.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainTabAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private List<Fragment> mFragments;

    public MainTabAdapter(@NonNull FragmentManager fm, @NonNull Context context) {
        super(fm);

        this.mContext =context;
        this.mFragments = new ArrayList<>();
    }

    public MainTabAdapter(@NonNull FragmentManager fm, @NonNull Context context, @NonNull List<Fragment> fragments) {
        super(fm);

        this.mContext = context;
        this.mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    public void addFragment(Fragment fragment) {
        mFragments.add(fragment);
    }
}
