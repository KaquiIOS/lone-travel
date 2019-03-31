package com.example.collpasingtest.views;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.collpasingtest.R;
import com.example.collpasingtest.adapters.MyRouteListAdapter;
import com.example.collpasingtest.interfaces.FirebaseContract;
import com.example.collpasingtest.interfaces.RecyclerViewOnClickListener;
import com.example.collpasingtest.models.Path;
import com.example.collpasingtest.models.PathType;
import com.example.collpasingtest.models.RouteInfo;
import com.example.collpasingtest.models.SearchInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class RouteSelectActivity extends AppCompatActivity {

    private RecyclerView mSelectionList;
    private MyRouteListAdapter adapter;

    private FirebaseUser mUser;
    private DatabaseReference mRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_select);

        adapter = new MyRouteListAdapter(this);
        adapter.setCanShare(false);
        adapter.setOnClickRecyclerViewListener(new RecyclerViewOnClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent();
                intent.putExtra("route", adapter.getRouteInfo(position));
                setResult(TravelAddActivity.ROUTE_SELECT_REQUEST, intent);
                finish();
            }
        });

        mSelectionList = findViewById(R.id.recycler_View_route_select);
        mSelectionList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mSelectionList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mSelectionList.setAdapter(adapter);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mRoute = FirebaseDatabase.getInstance().getReference(FirebaseContract.USERS)
                .child(mUser.getUid()).child(FirebaseContract.ROUTE);

        getLatestTravelList();
    }

    private void getLatestTravelList() {
        // 다이얼로그 시작

        mRoute.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adapter.clear();

                // 새로 받은 글 보여주기
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();

                while (iterator.hasNext()) {
                    DataSnapshot snapshot = iterator.next();

                    String content = snapshot.child("content").getValue(String.class);
                    String title = snapshot.child("title").getValue(String.class);
                    boolean isShared = snapshot.child("shared").getValue(Boolean.class);
                    Iterator<DataSnapshot> pathIterator = snapshot.child("routeList").getChildren().iterator();

                    ArrayList<PathType> pathList = new ArrayList<>();

                    while (pathIterator.hasNext()) {
                        DataSnapshot path = pathIterator.next();

                        if (path.getChildrenCount() == 6)
                            pathList.add(path.getValue(SearchInfo.class));
                        else
                            pathList.add(path.getValue(Path.class));

                    }
                    RouteInfo info = new RouteInfo(pathList, title, content, snapshot.getKey(), isShared);
                    adapter.addRouteList(info);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
