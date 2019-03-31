package com.example.collpasingtest.views;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.collpasingtest.R;
import com.example.collpasingtest.adapters.CityListAdapter;
import com.example.collpasingtest.interfaces.RecyclerViewOnClickListener;

public class CitySelectActivity extends AppCompatActivity {

    private RecyclerView citySelectActivity;
    private CityListAdapter adapter;

    private int selectedIdx = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_select);

        adapter = new CityListAdapter(this);
        adapter.setClickListener(new RecyclerViewOnClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent();
                intent.putExtra("info", TravelAddActivity.sCities[position]);
                setResult(SharedRouteDialog.CITY_SELECT_REQEUST, intent);
                finish();
            }
        });

        citySelectActivity = findViewById(R.id.recycler_view_city_select_activity);
        citySelectActivity.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        citySelectActivity.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        citySelectActivity.setAdapter(adapter);
    }
}
