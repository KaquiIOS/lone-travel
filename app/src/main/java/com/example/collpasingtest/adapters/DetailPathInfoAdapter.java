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
import com.example.collpasingtest.models.PassStop;
import com.example.collpasingtest.models.PathInfo;

import java.util.ArrayList;

public class DetailPathInfoAdapter extends RecyclerView.Adapter<DetailPathInfoAdapter.PassStopHolder> {

    private ArrayList<PathInfo> pathInfoList;
    private Context context;
    private String startStationName, endStationName;

    public DetailPathInfoAdapter(Context context, ArrayList<PathInfo> pathInfoList, String s, String e) {
        this.context = context;
        this.pathInfoList = pathInfoList;
        this.startStationName = s;
        this.endStationName = e;
    }

    @NonNull
    @Override
    public PassStopHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pass_stop, parent, false);
        return new PassStopHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PassStopHolder holder, int position) {

        final PathInfo curStop = pathInfoList.get(position);

        int hour = curStop.getSectionTime() / 60;
        int minute = curStop.getSectionTime() - hour * 60;

        holder.detailTime.setText(hour > 0 ? hour + " 시간 " + minute + " 분" : minute + " 분");


        // 걷기인 경우
        if(curStop.getTrafficType() == 3) {
            holder.trafficMarker.setImageResource(R.drawable.ic_item_stop_walk_24);

            if(position == 0) {
                PathInfo nextLocation = pathInfoList.get(position + 1);
                String type = nextLocation.getTrafficType() == 2 ? "정류장" : "역";
                holder.detailPath.setText(
                        startStationName + type + "까지 약 " +
                        (curStop.getDistance() > 1000 ? ((double)curStop.getDistance() / 1000) + "km" : curStop.getDistance() + "m") +
                        "걷기");
            } else if(position == pathInfoList.size() - 1) {

                holder.detailPath.setText(endStationName + "까지 약 " +
                        (curStop.getDistance() > 1000 ? ((double)curStop.getDistance() / 1000) + "km" : curStop.getDistance() + "m") +
                        "걷기");
            } else{
                PathInfo nextLocation = pathInfoList.get(position + 1);
                String type = nextLocation.getTrafficType() == 2 ? "정류장" : "역";
                holder.detailPath.setText(nextLocation.getPassStopList().get(0).getStationName() + type + "까지 약 " +
                        (curStop.getDistance() > 1000 ? ((double)curStop.getDistance() / 1000) + "km" : curStop.getDistance() + "m") +
                        "걷기");
            }
        }
        // 버스인 경우
        else if(curStop.getTrafficType() == 2) {
            holder.trafficMarker.setImageResource(R.drawable.ic_item_stop_bus_24);
            holder.detailStationNum.setText(curStop.getPassStopList().size() + "개 정류장 이동");

            int len = curStop.getPassStopList().size();
            holder.detailPath.setText(curStop.getPassStopList().get(0).getStationName() + " 승차 후,\n" +
                    curStop.getPassStopList().get(len - 1).getStationName() + "정류장에서 하차");
        }
        // 지하철인 경우
        else if(curStop.getTrafficType() == 1) {
            holder.trafficMarker.setImageResource(R.drawable.ic_item_stop_subway_24);
            holder.detailStationNum.setText(curStop.getPassStopList().size() + "개 역 이동");

            int len = curStop.getPassStopList().size();
            holder.detailPath.setText(curStop.getPassStopList().get(0).getStationName() + "역 승차 후,\n" +
                    curStop.getPassStopList().get(len - 1).getStationName() + "역에서 하차");
        }
    }

    @Override
    public int getItemCount() {
        return pathInfoList.size();
    }

    @Override
    public long getItemId(int position) {
        return pathInfoList.get(position).hashCode();
    }

    public static class PassStopHolder extends RecyclerView.ViewHolder {
        public ImageView trafficMarker;
        public TextView detailPath, detailTime, detailStationNum;

        public PassStopHolder(View itemView) {
            super(itemView);
            trafficMarker = itemView.findViewById(R.id.image_view_item_stop_mark);
            detailPath = itemView.findViewById(R.id.text_view_item_stop_path);
            detailTime = itemView.findViewById(R.id.text_view_item_stop_time);
            detailStationNum = itemView.findViewById(R.id.text_view_item_stop_station_num);
        }
    }
}
