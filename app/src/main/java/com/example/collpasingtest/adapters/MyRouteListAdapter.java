package com.example.collpasingtest.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.collpasingtest.R;
import com.example.collpasingtest.interfaces.FirebaseContract;
import com.example.collpasingtest.interfaces.RecyclerViewOnClickListener;
import com.example.collpasingtest.models.Path;
import com.example.collpasingtest.models.PathInfo;
import com.example.collpasingtest.models.PathType;
import com.example.collpasingtest.models.RouteInfo;
import com.example.collpasingtest.models.SearchInfo;
import com.example.collpasingtest.models.SharedRouteInfo;
import com.example.collpasingtest.views.MainActivity;
import com.example.collpasingtest.views.RouteMapActivity;
import com.example.collpasingtest.views.SharedRouteDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class MyRouteListAdapter extends RecyclerView.Adapter<MyRouteListAdapter.RouteListVH> {

    private ArrayList<RouteInfo> list;
    private Context context;
    private RecyclerViewOnClickListener listener;
    private boolean canShare = true;
    private SharedRouteDialog sharedDialog = null;

    private DatabaseReference sharedRouteRef, tagRef, regionRef, mUserRef;

    public MyRouteListAdapter(Context context) {
        this.context = context;
        this.list = new ArrayList<>();
        this.sharedRouteRef = FirebaseDatabase.getInstance().getReference("sharedRoute");
        this.tagRef = FirebaseDatabase.getInstance().getReference("tag");
        this.regionRef = FirebaseDatabase.getInstance().getReference("region");
        this.mUserRef = FirebaseDatabase.getInstance().getReference(FirebaseContract.USERS)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(FirebaseContract.ROUTE);
    }

    public void setOnClickRecyclerViewListener(RecyclerViewOnClickListener listener) {
        this.listener = listener;
    }

    public void setCanShare(boolean share) {
        this.canShare = share;
    }

    @NonNull
    @Override
    public RouteListVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_added_route_list, parent, false);

        return new RouteListVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteListVH holder, final int position) {
        final RouteInfo info = list.get(position);

        holder.titleTextView.setText(info.getTitle());
        holder.contentTextView.setText(info.getContent());
        holder.openMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // show map with info
                Intent intent = new Intent(context, RouteMapActivity.class);
                intent.putExtra("info", info);
                context.startActivity(intent);
            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(position);
            }
        });

        if(canShare) {
            holder.shareBtn.setVisibility(View.VISIBLE);
            holder.shareBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(info.isShared()) {
                        Snackbar.make(((MainActivity)context).getWindow().getDecorView(), "이미 공유한 여행경로 입니다 !", Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    sharedDialog = new SharedRouteDialog(context);

                    sharedDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            // 데이터를 다 받아오고나서 처리
                            // 공유해주기
                            if(sharedDialog.isEnrolled()) {
                                String title = sharedDialog.getTitle();
                                String content = sharedDialog.getContent();
                                final String selectedCity =sharedDialog.getSelectedCity();
                                final ArrayList<String> tags = sharedDialog.getTags();
                                ArrayList<PathType> routeInfo = list.get(position).getRouteList();
                                ArrayList<SearchInfo> searchInfoList = new ArrayList<>();
                                Iterator<PathType> it = routeInfo.iterator();

                                while(it.hasNext()) {
                                    PathType t = it.next();
                                    if(t instanceof SearchInfo)
                                        searchInfoList.add((SearchInfo)t);
                                }

                                final String newKey = sharedRouteRef.push().getKey();

                                // 파이어베이스에 저장하기
                                SharedRouteInfo sharedRouteInfo = new SharedRouteInfo(
                                        FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                        title, content, newKey, selectedCity, tags, searchInfoList, new Date(), 0, 0
                                );

                                sharedRouteRef.child(newKey).setValue(sharedRouteInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()) {
                                            Snackbar.make(((MainActivity)context).getWindow().getDecorView(), "경로 공유가 되었습니다 !", Snackbar.LENGTH_SHORT).show();

                                            int len = tags.size();

                                            for(int j = 0; j < len; ++j) {
                                                tagRef.child(tags.get(j)).child(newKey).setValue(newKey);
                                            }
                                            regionRef.child(selectedCity).child(newKey).setValue(newKey);
                                            return;
                                        }
                                    }
                                });

                                list.get(position).setShared(true);
                                mUserRef.child(info.getRouteID()).child("shared").setValue(true);
                            }
                        }
                    });

                    sharedDialog.show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class RouteListVH extends RecyclerView.ViewHolder {
        public TextView titleTextView, contentTextView;
        public ImageButton shareBtn, openMapBtn;

        public RouteListVH(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_view_item_route_list_title);
            contentTextView = itemView.findViewById(R.id.text_view_item_route_list_content);
            shareBtn = itemView.findViewById(R.id.image_btn_item_route_list_share);
            openMapBtn = itemView.findViewById(R.id.image_btn_item_open_map);
        }
    }

    public ArrayList<RouteInfo> getRouteInfoList() {
        return list;
    }

    public RouteInfo getRouteInfo(int pos) {
        return list.get(pos);
    }

    public void clear() {
        list.clear();
    }

    public void remove(RouteInfo info) {
        list.remove(info);
    }

    public void addRouteList(RouteInfo info) {
        list.add(info);
    }

    public void updateRouteList(RouteInfo info) {
        int idx = 0;
        for (RouteInfo cur : list) {
            if (cur.getRouteID().equals(info.getRouteID())) {
                list.set(idx, info);
                return;
            }
            idx++;
        }
        // 없는 경우엔
        addRouteList(info);
    }

    public void removeItem(String routeID) {

        Iterator<RouteInfo> it = list.iterator();

        while (it.hasNext()) {
            if (it.next().getRouteID().equals(routeID)) {
                it.remove();
                return;
            }
        }
    }

    public ArrayList<RouteInfo> getList() {
        return list;
    }

    public void setList(ArrayList<RouteInfo> list) {
        this.list = list;
    }


    public void setSelectedCity(String str) {
        if(sharedDialog != null)
            sharedDialog.setSelectedCity(str);
    }
}
