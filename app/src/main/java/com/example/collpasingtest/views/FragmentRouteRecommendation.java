package com.example.collpasingtest.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.collpasingtest.adapters.RouteRecommendationAdapter;
import com.example.collpasingtest.adapters.TagAdapter;
import com.example.collpasingtest.R;
import com.example.collpasingtest.interfaces.FirebaseContract;
import com.example.collpasingtest.interfaces.RecyclerViewOnClickListener;
import com.example.collpasingtest.models.PathType;
import com.example.collpasingtest.models.RouteInfo;
import com.example.collpasingtest.models.SharedRouteInfo;
import com.google.android.gms.internal.firebase_database.zzch;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class FragmentRouteRecommendation extends Fragment {

    public static final int CITY_SELECT_REQUEST = 8111;

    private Iterator<DataSnapshot> iterator;
    private int mCurrentPostNum = 0, mTotalNum = 0;

    private MainActivity mParentActivity;

    private RouteRecommendationAdapter mRecAdapter;
    private TagAdapter mTagAdapter;
    private EditText mTagEdt;
    //private Button citySelectBtn;

    private FirebaseUser mUser;
    private DatabaseReference mRouteRef, mSharedRouteRef;

    private DatabaseReference mTagRef, mRegionRef;

    int current = 0, i, total = 0, currentOrder = 0;

    private ArrayList<String> temp = null;

    public FragmentRouteRecommendation() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recommendation, container, false);

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mRouteRef = FirebaseDatabase.getInstance().getReference(FirebaseContract.USERS)
                .child(mUser.getUid()).child(FirebaseContract.ROUTE);
        mSharedRouteRef = FirebaseDatabase.getInstance().getReference("sharedRoute");

        mTagRef = FirebaseDatabase.getInstance().getReference("tag");
        mRegionRef = FirebaseDatabase.getInstance().getReference("region");


        Button searchBtn = root.findViewById(R.id.btn_route_rec_search);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                temp = new ArrayList<>();
                searchRoute();
            }
        });

        mTagEdt = root.findViewById(R.id.edit_text_route_rec_tags);
        mTagEdt.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; ++i) {
                    if (Character.isSpaceChar(source.charAt(i))) {
                        mTagAdapter.addTag(mTagEdt.getText().toString().trim());
                        mTagAdapter.notifyDataSetChanged();
                        mTagEdt.setText("");
                        return null;
                    }
                }
                return null;
            }
        }});

        mTagAdapter = new TagAdapter(mParentActivity);
        mTagAdapter.setClickListener(new RecyclerViewOnClickListener() {
            @Override
            public void onClick(int position) {
                mTagAdapter.removeTag(position);
                mTagAdapter.notifyDataSetChanged();
            }
        });

        RecyclerView tagRecyclerView = root.findViewById(R.id.recycler_view_route_rec_tag);
        tagRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        tagRecyclerView.setAdapter(mTagAdapter);

        mRecAdapter = new RouteRecommendationAdapter(mParentActivity);
        RecyclerView recRecyclerView = root.findViewById(R.id.recycler_view_route_rec_list);
        recRecyclerView.setLayoutManager(new LinearLayoutManager(root.getContext(), LinearLayoutManager.VERTICAL, false));
        recRecyclerView.setAdapter(mRecAdapter);

        recRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (recyclerView.canScrollVertically(1)) {
                    if (temp != null) {
                        fetchSearchedSharedInfo();
                    } else {
                        fetchSharedInfo();
                    }
                }
            }
        });

        getLatestList();

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


    private void getLatestList() {

        mRecAdapter.clear();
        mRecAdapter.notifyDataSetChanged();

        Query orderQuery;

        switch (currentOrder) {
            case 0:
                orderQuery = mSharedRouteRef.orderByChild("enrollDate");
                break;
            case 1:
                orderQuery = mSharedRouteRef.orderByChild("likeNum");
                break;
            case 2:
                orderQuery = mSharedRouteRef.orderByChild("shareNum");
                break;
            default:
                orderQuery = mSharedRouteRef.orderByChild("enrollDate");
                break;
        }

        orderQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) return;
                iterator = dataSnapshot.getChildren().iterator();
                mTotalNum = mTotalNum + (int) dataSnapshot.getChildrenCount();
                fetchSharedInfo();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void fetchSearchedSharedInfo() {

        for (int i = 0; i < 5 && mCurrentPostNum < mTotalNum; ++i, mCurrentPostNum++) {
            mSharedRouteRef.child(temp.get(mCurrentPostNum)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    SharedRouteInfo info = dataSnapshot.getValue(SharedRouteInfo.class);
                    mRecAdapter.addRoute(info);
                    mRecAdapter.notifyItemChanged(mCurrentPostNum);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }

    private void fetchSharedInfo() {
        int preNum = mCurrentPostNum;
        for (int i = 0; i < 5 && mCurrentPostNum < mTotalNum; ++i, ++mCurrentPostNum) {
            SharedRouteInfo d = iterator.next().getValue(SharedRouteInfo.class);
            mRecAdapter.addRoute(d);
        }
        mRecAdapter.notifyDataSetChanged();
    }


    private void searchRoute() {

        current = mCurrentPostNum = 0;
        total = mTotalNum = 0;

        final HashSet<String> tagSearchResult = new HashSet<>();

        final int len = mTagAdapter.getItemCount();

        for (i = 0; i < len; ++i) {
            mTagRef.child(mTagAdapter.getTagList().get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();
                    while (it.hasNext()) {
                        DataSnapshot t = it.next();
                        tagSearchResult.add(t.getValue(String.class));
                    }
                    current++;
                    if (current == len) {
                        temp = new ArrayList<>(tagSearchResult);
                        mTotalNum = temp.size();
                        mRecAdapter.clear();
                        mRecAdapter.notifyDataSetChanged();
                        fetchSearchedSharedInfo();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
    }
}
