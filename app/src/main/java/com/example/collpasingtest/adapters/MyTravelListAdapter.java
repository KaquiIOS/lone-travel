package com.example.collpasingtest.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.collpasingtest.R;
import com.example.collpasingtest.interfaces.RecyclerViewOnClickListener;
import com.example.collpasingtest.models.MyTrip;

import java.util.ArrayList;

public class MyTravelListAdapter extends RecyclerView.Adapter<MyTravelListAdapter.MyTravel> {

    private ArrayList<MyTrip> myTravelList;
    private Context mContext;
    private RecyclerViewOnClickListener listener;

    public MyTravelListAdapter(Context mContext) {
        this.mContext = mContext;
        this.myTravelList = new ArrayList<>();
    }

    public void setListener(RecyclerViewOnClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyTravel onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_my_trip, parent, false);
        return new MyTravel(view);
    }

    public void clear() {
        myTravelList.clear();
    }

    @Override
    public void onBindViewHolder(@NonNull MyTravel holder, final int position) {

        MyTrip item = myTravelList.get(position);

        holder.titleTextView.setText(String.format("%s 에 시작하는 %s 여행",
                item.getParsedStartDate(), item.getRegion()));

        holder.endDateTextView.setText(String.format("~ %s 일 까지", item.getParsedEndDate()));

        if(listener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(position);
                }
            });
        }
    }

    public MyTrip getItem(int pos) {
        return myTravelList.get(pos);
    }

    public int removeItem(String key) {
        int idx = 0;
        for(MyTrip t : myTravelList) {
            if (t.getTripKey().equals(key)) {
                myTravelList.remove(idx);
                return idx;
            }
            idx++;
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return myTravelList.size();
    }

    public void add(MyTrip item) {
        myTravelList.add(item);
    }

    public static class MyTravel extends RecyclerView.ViewHolder {
        public TextView titleTextView, endDateTextView;

        public MyTravel(View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.text_view_item_my_trip_title);
            endDateTextView = itemView.findViewById(R.id.text_view_item_my_trip_end);
        }
    }
}
