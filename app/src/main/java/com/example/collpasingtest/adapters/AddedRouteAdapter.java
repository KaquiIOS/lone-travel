package com.example.collpasingtest.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.collpasingtest.R;
import com.example.collpasingtest.interfaces.RecyclerViewItemTouchCallback;
import com.example.collpasingtest.interfaces.RecyclerViewOnClickListener;
import com.example.collpasingtest.models.Path;
import com.example.collpasingtest.models.PathInfo;
import com.example.collpasingtest.models.PathType;
import com.example.collpasingtest.models.SearchInfo;

import java.util.ArrayList;
import java.util.Collections;

public class AddedRouteAdapter extends RecyclerView.Adapter<AddedRouteAdapter.AddedRouteVH>
        implements RecyclerViewItemTouchCallback.ItemTouchHelperContract {

    private ArrayList<PathType> arrayList;
    private Context context;
    private RecyclerViewOnClickListener clickListener;
    private boolean canMove = true;
    private boolean canRemove = true;

    private static int red_color, blue_color;

    public AddedRouteAdapter(Context context) {
        this.context = context;
        arrayList = new ArrayList<>();

        red_color = context.getColor(R.color.main_red);
        blue_color = context.getColor(R.color.royal_blue);
    }

    public void setRecyclerViewOnClickListener(RecyclerViewOnClickListener listener) {
        this.clickListener = listener;
    }

    @Override
    public long getItemId(int position) {
        if (arrayList.get(position) instanceof Path)
            return ((Path) arrayList.get(position)).hashCode();
        return
                ((SearchInfo) arrayList.get(position)).hashCode();
    }

    public PathType getItem(int pos) {
        return arrayList.get(pos);
    }

    public boolean isCanMove() {
        return canMove;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    public void addLocation(PathType info) {
        arrayList.add(info);
    }

    public void addLocations(ArrayList<PathType> info) {
        for (int i = 0; i < info.size(); ++i) {
            arrayList.add(info.get(i));
        }
    }

    public boolean isCanRemove() {
        return canRemove;
    }

    public void setCanRemove(boolean canRemove) {
        this.canRemove = canRemove;
    }

    public void setLocations(ArrayList<PathType> info) {
        this.arrayList = new ArrayList<>(info);
    }

    public void clear() {
        arrayList.clear();
    }

    public void remove(int pos) {
        arrayList.remove(pos);
    }

    @Override
    public void onRowMoved(int fromPos, int toPos) {

        if (arrayList.get(fromPos) instanceof Path || !canMove) {
            // error 메시지 보여주기
            return;
        }

        //Collections.swap(arrayList, fromPos, toPos);
        if (fromPos < toPos) {
            for (int i = fromPos; i < toPos; i++) {
                Collections.swap(arrayList, i, i + 1);
            }
        } else {
            for (int i = fromPos; i > toPos; i--) {
                Collections.swap(arrayList, i, i - 1);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onRowSelected(AddedRouteVH holder) {
        if (canMove)
            holder.itemView.setBackgroundColor(Color.GRAY);
    }

    @Override
    public void onRowClear(AddedRouteVH holder) {
        if (canMove)
            holder.itemView.setBackgroundColor(Color.WHITE);
    }

    @NonNull
    @Override
    public AddedRouteVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_added_route, parent, false);
        return new AddedRouteVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AddedRouteVH holder, final int position) {

        if (arrayList.get(position) instanceof SearchInfo) {

            holder.searchInfoContainer.setVisibility(View.VISIBLE);
            holder.pathContainer.setVisibility(View.GONE);

            SearchInfo info = (SearchInfo) arrayList.get(position);
            // recyclerview에 넣기
            holder.mTitle.setText(info.getTitle());
            holder.mAddr.setText(info.getAddress());
            holder.mBizName.setText(info.getBizName());
            if(info.getTelNo().equals(" "))
                holder.mTelNo.setVisibility(View.GONE);
            else
                holder.mTelNo.setText(info.getTelNo());
            if (canRemove) {
                holder.mRemoveBtn.setVisibility(View.VISIBLE);
                holder.mRemoveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        clickListener.onClick(position);
                    }
                });
            } else {
                holder.mRemoveBtn.setVisibility(View.GONE);
            }
        }
        // 경로 추천 카드인 경우
        else if (arrayList.get(position) instanceof Path) {

            holder.searchInfoContainer.setVisibility(View.GONE);
            holder.pathContainer.setVisibility(View.VISIBLE);

            Path path = (Path) arrayList.get(position);

            if (path.getPayment() > 0)
                holder.mPayment.setText(String.valueOf(path.getPayment()) + "원");
            else
                holder.mPayment.setText("요금 정보 미제공");

            final int hour = path.getTotalTime() / 60;
            int minute = path.getTotalTime() - hour * 60;

            StringBuilder timeStr = new StringBuilder();
            if (hour != 0) timeStr.append(hour + "시간 ");
            timeStr.append(minute + "분 ");

            holder.mRouteTime.setText("약 " + timeStr.toString() + "소요");

            String pubText = "";

            if (path.getBusStationCount() != 0)
                pubText += "버스 정류장 " + (path.getBusStationCount() + 1) + "개 ";
            if (path.getSubStationCount() != 0)
                pubText += "지하철 정류장 " + (path.getSubStationCount() + 1) + "개";

            holder.mTotalStation.setText(pubText);

            holder.mTotalDist.setText("총 거리 : " + String.format("%.2f", (double) path.getTotalDistance() / 1000) + "km");

            ArrayList<PathInfo> pathList = path.getPathInfoList();

            if (pathList != null) {
                int len = pathList.size();
                StringBuilder pathStr = new StringBuilder();
                for (int i = 0; i < len; ++i) {

                    PathInfo stop = pathList.get(i);

                    // 마지막 경로에서는 하차만 생각함
                    if (i == len - 1) {
                        pathStr.append(path.getEndStationName() + " 정류장 하차");
                        break;
                    }

                    // 지하철인 경우
                    if (stop.getTrafficType() == 1) {
                        String[] temp = stop.getVehicleName().split(" ");
                        pathStr.append(temp[temp.length - 1] + " " + lastCharChecker(stop.getPassStopList().get(0).getStationName()) + "역 승차 ->\n");
                    }
                    // 버스인 경우
                    else if (stop.getTrafficType() == 2) {
                        pathStr.append(stop.getVehicleName() + "번 (" + stop.getPassStopList().get(0).getStationName() + "역 승차) ->\n");
                    }
                }
                holder.mRouteList.setText(pathStr.toString());

                SearchInfo info = (SearchInfo) arrayList.get(position + 1);
                DetailPathInfoAdapter adapter = new DetailPathInfoAdapter(context, pathList, path.getFirstStationName(), info.getTitle());

                holder.routeList.setAdapter(adapter);
                holder.routeList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                holder.routeList.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));


                // 루트가 존재하는 경우엔 상세 보여주기 버튼 활성화
                holder.detailRouteBtn.setVisibility(View.VISIBLE);
                holder.detailRouteBtn.setImageResource(R.drawable.ic_added_route_item_collapse_24);
                holder.detailRouteBtn.setTag(R.drawable.ic_added_route_item_collapse_24);

                holder.detailRouteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int curState = (int) holder.detailRouteBtn.getTag();
                        //닫힌 상태
                        if (curState == R.drawable.ic_added_route_item_collapse_24) {
                            holder.routeList.setVisibility(View.VISIBLE);
                            holder.detailRouteBtn.setImageResource(R.drawable.ic_added_route_item_expand_24);
                            holder.detailRouteBtn.setTag(R.drawable.ic_added_route_item_expand_24);

                        }
                        // 열린 상태
                        else {
                            holder.routeList.setVisibility(View.GONE);
                            holder.detailRouteBtn.setImageResource(R.drawable.ic_added_route_item_collapse_24);
                            holder.detailRouteBtn.setTag(R.drawable.ic_added_route_item_collapse_24);
                        }
                    }
                });
            } else {
                holder.mRouteList.setText("대중 교통 경로 미제공");
                holder.detailRouteBtn.setVisibility(View.GONE);
            }
        }
    }

    private String lastCharChecker(String stationName) {

        int len = stationName.length();

        if (stationName.substring(len - 1).equals("역"))
            return stationName.substring(0, len - 1);
        return stationName;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class AddedRouteVH extends RecyclerView.ViewHolder {
        public ConstraintLayout searchInfoContainer;
        public TextView mTitle, mBizName, mAddr, mTelNo;
        public ImageButton mRemoveBtn;

        public ConstraintLayout pathContainer;
        public TextView mPayment, mRouteTime, mTotalStation, mTotalDist, mRouteList;
        public RecyclerView routeList;
        public ImageButton detailRouteBtn;

        public AddedRouteVH(View itemView) {
            super(itemView);

            searchInfoContainer = itemView.findViewById(R.id.container_route_add_location);
            mRemoveBtn = itemView.findViewById(R.id.image_btn_route_add_remove);
            mTitle = itemView.findViewById(R.id.text_view_item_added_route_title);
            mBizName = itemView.findViewById(R.id.text_view_item_added_route_kind);
            mAddr = itemView.findViewById(R.id.text_view_item_added_route_address);
            mTelNo = itemView.findViewById(R.id.text_view_item_added_route_tel_no);

            pathContainer = itemView.findViewById(R.id.container_route_add_path);
            mPayment = itemView.findViewById(R.id.text_view_item_added_route_payment);
            mRouteTime = itemView.findViewById(R.id.text_view_item_added_route_time);
            mTotalStation = itemView.findViewById(R.id.text_view_item_added_route_total_station);
            mTotalDist = itemView.findViewById(R.id.text_view_item_added_route_distance);
            mRouteList = itemView.findViewById(R.id.text_view_item_added_route_path);
            routeList = itemView.findViewById(R.id.recycler_view_item_added_route_route);
            detailRouteBtn = itemView.findViewById(R.id.image_btn_item_added_route_detail_route);
        }
    }
}
