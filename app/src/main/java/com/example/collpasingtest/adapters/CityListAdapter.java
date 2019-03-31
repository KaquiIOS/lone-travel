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
import com.example.collpasingtest.views.TravelAddActivity;

public class CityListAdapter extends RecyclerView.Adapter<CityListAdapter.CityItemHolder> {

    private Context mContext;
    private RecyclerViewOnClickListener clickListener;

    public CityListAdapter(Context context) {
        this.mContext = context;
    }

    public void setClickListener(RecyclerViewOnClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public CityItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_city, parent, false);
        return new CityItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityItemHolder holder, final int position) {
        if(clickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onClick(position);
                }
            });
        }

        holder.cityImageView.setImageResource(TravelAddActivity.sImageList[position]);
        holder.textView.setText(TravelAddActivity.sCities[position]);
    }

    @Override
    public int getItemCount() {
        return TravelAddActivity.sImageList.length;
    }

    public static class CityItemHolder extends RecyclerView.ViewHolder {

        public ImageView cityImageView;
        public TextView textView;

        public CityItemHolder(View itemView) {
            super(itemView);
            cityImageView = itemView.findViewById(R.id.image_view_item_city);
            textView = itemView.findViewById(R.id.text_view_item_name);
        }
    }
}
