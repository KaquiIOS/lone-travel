package com.example.collpasingtest.views;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.collpasingtest.R;
import com.example.collpasingtest.adapters.CityListAdapter;
import com.example.collpasingtest.customviews.RecyclerViewItemClickListener;
import com.example.collpasingtest.interfaces.TravelAddInterface;

public class CitySelectFragment extends Fragment {

    private CityListAdapter mAdapter;
    private TravelAddActivity mParentActivity;
    private TravelAddInterface mTravelAddInterface;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof TravelAddActivity) {
            mParentActivity = (TravelAddActivity) context;
            mTravelAddInterface = (TravelAddInterface) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_city_select, container, false);

        mAdapter = new CityListAdapter(getContext());
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view_city_select);
        recyclerView.setLayoutManager(new LinearLayoutManager(mParentActivity, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(mParentActivity, new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                mTravelAddInterface.setCity(pos);
            }
        }));

        return root;
    }
}
