package com.example.collpasingtest.interfaces;

import android.view.View;

import com.example.collpasingtest.adapters.TravelListAdapter;

public interface OnTravelRecyclerViewClickListener {
    // RecyclerView Item Click Event Handle Function
    void setOnClickListener(TravelListAdapter.TravelRecyclerViewHolder holder, View view, int pos);
}
