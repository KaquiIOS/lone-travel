package com.example.collpasingtest.views;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.collpasingtest.R;
import com.example.collpasingtest.interfaces.LocationItemClickListener;
import com.example.collpasingtest.models.SearchInfo;

public class LocationInfoFragment extends Fragment {

    private Context context;
    private LocationItemClickListener listener;

    public LocationInfoFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

        if(context instanceof RouteAddActivity)
            listener = (LocationItemClickListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)(inflater.inflate(R.layout.item_location, container, false));

        SearchInfo info = (SearchInfo) getArguments().getSerializable("info");

        TextView titleTextView = root.findViewById(R.id.text_view_item_location_title);
        titleTextView.setText(info.getTitle());

        TextView bizTextView = root.findViewById(R.id.text_view_item_location_kind);
        bizTextView.setText(info.getBizName());

        TextView addrTextView = root.findViewById(R.id.text_view_item_added_route_address);
        addrTextView.setText(info.getAddress());

        TextView telNoTextView = root.findViewById(R.id.text_view_item_added_route_tel_no);
        telNoTextView.setText(info.getTelNo());

        Button addBtn = root.findViewById(R.id.btn_item_location_add);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.addLocationItem((SearchInfo)getArguments().get("info"));
            }
        });
        Button cancelBtn = root.findViewById(R.id.btn_item_location_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.cancelLocationItem();
            }
        });

        return root;
    }
}
