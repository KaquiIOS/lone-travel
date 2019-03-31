package com.example.collpasingtest.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.collpasingtest.adapters.MyRouteListAdapter;
import com.example.collpasingtest.interfaces.FirebaseContract;
import com.example.collpasingtest.interfaces.RecyclerViewOnClickListener;
import com.example.collpasingtest.models.Path;
import com.example.collpasingtest.models.PathType;
import com.example.collpasingtest.models.RouteInfo;
import com.example.collpasingtest.models.SearchInfo;
import com.example.collpasingtest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;


public class FragmentRoute extends Fragment {

    private MainActivity mParentActivity;
    private MyRouteListAdapter adapter;

    private FirebaseUser mUser;
    private DatabaseReference mRoute, mGlobalRoute;


    public FragmentRoute() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_route, container, false);

        adapter = new MyRouteListAdapter(getContext());
        adapter.setCanShare(true);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view_fragment_route);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        adapter.setOnClickRecyclerViewListener(new RecyclerViewOnClickListener() {
            @Override
            public void onClick(int position) {

                RouteInfo routeInfo = adapter.getRouteInfo(position);

                Intent intent = new Intent(mParentActivity, SelectedRouteActivity.class);
                intent.putExtra("info", routeInfo);
                getActivity().startActivityForResult(intent, MainActivity.SELECTED_ROUTE_REQUEST);
            }
        });

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mRoute = FirebaseDatabase.getInstance().getReference(FirebaseContract.USERS)
                .child(mUser.getUid()).child(FirebaseContract.ROUTE);

        mRoute.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                adapter.addRouteList(parseRouteInfo(dataSnapshot));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        getLatestTravelList();

        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.mParentActivity = (MainActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mParentActivity = null;
    }

    public ArrayList<RouteInfo> getRouteInfoList() {
        return adapter.getRouteInfoList();
    }

    public void addItem(final RouteInfo info) {

        //Firebase에 등록하기
        String newKey = mRoute.child(mUser.getUid()).push().getKey();

        info.setRouteID(newKey);

        mRoute.child(newKey).setValue(info).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
    }

    public void updateItem(final RouteInfo info) {
        adapter.updateRouteList(info);
        adapter.notifyDataSetChanged();
    }

    public void deleteItem(final String key) {
        mRoute.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    adapter.removeItem(key);
                    adapter.notifyDataSetChanged();
                }
            }
        });
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
                    adapter.addRouteList(parseRouteInfo(iterator.next()));
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private RouteInfo parseRouteInfo(DataSnapshot dataSnapshot) {

        String content = dataSnapshot.child("content").getValue(String.class);
        String title = dataSnapshot.child("title").getValue(String.class);
        boolean isShared = dataSnapshot.child("shared").getValue(Boolean.class);
        Iterator<DataSnapshot> pathIterator = dataSnapshot.child("routeList").getChildren().iterator();

        ArrayList<PathType> pathList = new ArrayList<>();

        while (pathIterator.hasNext()) {
            DataSnapshot path = pathIterator.next();

            if (path.getChildrenCount() == 6)
                pathList.add(path.getValue(SearchInfo.class));
            else
                pathList.add(path.getValue(Path.class));

        }
        return new RouteInfo(pathList, title, content, dataSnapshot.getKey(), isShared);
    }

    public void sendSelectedCity(String str) {
        // adapter 에게 보내주기
        adapter.setSelectedCity(str);
    }

    private void shareMyRouteList() {
        // 공유한 사람 키 =>
        // 공유한 루트   =>
        // 좋아요 수
    }

}
