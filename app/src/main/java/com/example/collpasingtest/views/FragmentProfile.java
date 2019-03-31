package com.example.collpasingtest.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.collpasingtest.R;
import com.example.collpasingtest.adapters.MyTravelListAdapter;
import com.example.collpasingtest.interfaces.FirebaseContract;
import com.example.collpasingtest.interfaces.RecyclerViewOnClickListener;
import com.example.collpasingtest.models.MyTrip;
import com.example.collpasingtest.models.Path;
import com.example.collpasingtest.models.PathType;
import com.example.collpasingtest.models.RouteInfo;
import com.example.collpasingtest.models.SearchInfo;
import com.example.collpasingtest.models.Travel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;


public class FragmentProfile extends Fragment {

    private MainActivity mParentActivity;
    private FirebaseUser mUser;
    private DatabaseReference mUserRef, mTravelRef;
    private MyTravelListAdapter adapter;

    private EventHandler handler;

    public FragmentProfile() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_profile, container, false);

        handler = new EventHandler();

        ImageView profileView = root.findViewById(R.id.image_view_profile_user);
        Glide.with(mParentActivity).load(mUser.getPhotoUrl()).apply(new RequestOptions().fitCenter()).into(profileView);

        TextView nameTextView = root.findViewById(R.id.text_view_profile_name);
        nameTextView.setText(mUser.getDisplayName());

        adapter = new MyTravelListAdapter(mParentActivity);
        adapter.setListener(new RecyclerViewOnClickListener() {
            @Override
            public void onClick(int position) {
                // 해당 위치로 이동하기
                mTravelRef.child(adapter.getItem(position).getTripKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Travel travel = new Travel();
                        travel.setContent(dataSnapshot.child("content").getValue(String.class));
                        travel.setPhotoPath(dataSnapshot.child("photoPath").getValue(String.class));
                        travel.setRegion(dataSnapshot.child("region").getValue(String.class));
                        travel.setTravelStartDate(dataSnapshot.child("travelStartDate").getValue(Date.class));
                        travel.setTravelEndDate(dataSnapshot.child("travelEndDate").getValue(Date.class));
                        travel.setWriteDate(dataSnapshot.child("writeDate").getValue(Date.class));
                        travel.setWriterUid(dataSnapshot.child("writerUid").getValue(String.class));
                        travel.setContentId(dataSnapshot.getKey());
                        travel.setPhotoPath(dataSnapshot.child("photoPath").getValue(String.class));

                        // ArrayList만 빼오기
                        RouteInfo info = new RouteInfo();
                        DataSnapshot routeSnapshot = dataSnapshot.child("routeInfo");
                        if (routeSnapshot != null) {
                            info.setRouteID(routeSnapshot.child("routeID").getValue(String.class));
                            info.setContent(routeSnapshot.child("content").getValue(String.class));
                            info.setTitle(routeSnapshot.child("title").getValue(String.class));

                            ArrayList<PathType> routeList = new ArrayList<>();

                            Iterator<DataSnapshot> iterator = routeSnapshot.child("routeList").getChildren().iterator();
                            while (iterator.hasNext()) {
                                DataSnapshot path = iterator.next();

                                if (path.getChildrenCount() == 6)
                                    routeList.add(path.getValue(SearchInfo.class));
                                else
                                    routeList.add(path.getValue(Path.class));
                            }
                            info.setRouteList(routeList);
                        }
                        travel.setRouteInfo(info);

                        Intent intent = new Intent(mParentActivity, DetailTravelInfoActivity.class);
                        intent.putExtra("travel_info", travel);
                        getActivity().startActivityForResult(intent, MainActivity.TRAVEL_DETAIL_REQUEST);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view_profile_my_trip);
        recyclerView.setLayoutManager(new LinearLayoutManager(mParentActivity, LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(mParentActivity, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.clear();
        mUserRef.addChildEventListener(handler);
    }

    @Override
    public void onPause() {
        super.onPause();
        mUserRef.removeEventListener(handler);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mParentActivity = (MainActivity) context;

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference(FirebaseContract.USERS)
                .child(mUser.getUid()).child(FirebaseContract.ROOM);

        mTravelRef = FirebaseDatabase.getInstance().getReference(FirebaseContract.TRAVEL);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        this.mParentActivity = null;
    }

    private class EventHandler implements ChildEventListener {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            final String travelId = dataSnapshot.child("chatRoomId").getValue(String.class);

            mTravelRef.child(travelId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Date startDate = dataSnapshot.child("travelStartDate").getValue(Date.class);
                    Date endDate = dataSnapshot.child("travelEndDate").getValue(Date.class);
                    String region = dataSnapshot.child("region").getValue(String.class);

                    MyTrip t = new MyTrip(travelId, region, startDate, endDate);
                    adapter.add(t);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            // 방이 삭제되는 경우
            String travelId = dataSnapshot.child("chatRoomId").getValue(String.class);

            int idx = adapter.removeItem(travelId);
            if(idx != -1)
                adapter.notifyDataSetChanged();
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) { }
    }
}
