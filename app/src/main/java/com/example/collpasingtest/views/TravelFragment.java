package com.example.collpasingtest.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.collpasingtest.interfaces.FirebaseContract;
import com.example.collpasingtest.adapters.TravelListAdapter;
import com.example.collpasingtest.R;
import com.example.collpasingtest.interfaces.OnTravelRecyclerViewClickListener;
import com.example.collpasingtest.models.Path;
import com.example.collpasingtest.models.PathType;
import com.example.collpasingtest.models.Room;
import com.example.collpasingtest.models.RouteInfo;
import com.example.collpasingtest.models.SearchInfo;
import com.example.collpasingtest.models.Travel;
import com.example.collpasingtest.models.User;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class TravelFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final int TRAVEL_ADD_REQUEST = 101, TRAVEL_DETAIL_REQUEST = 102;

    private static final SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private TravelListAdapter mViewAdapter;
    private MainActivity mParentActivity;

    private SwipeRefreshLayout mMainContainer;

    // FirebaseObject get
    private FirebaseDatabase mFirebaseDB;
    private DatabaseReference mMemRef, mUserRef, mTravelRef;
    private FirebaseUser mUser;

    private int currentOrder = 0;
    private int []sOrderImage = new int[]{R.drawable.ic_time_order_24, R.drawable.ic_like_order_24};
    private String currentCity = "도시 선택";

    private Iterator<DataSnapshot> travelListSnapshot;
    private int mCurrentPostNum = 0, mTotalNum = 0;

    private Query query = null;
    Button citySelectBtn;

    public TravelFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mParentActivity = (MainActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Fragment Layout Inflate
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_travel, container, false);

        // Fireabse
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mFirebaseDB = FirebaseDatabase.getInstance();
        mMemRef = mFirebaseDB.getReference(FirebaseContract.CHAT_MEMBER);
        mUserRef = mFirebaseDB.getReference(FirebaseContract.USERS);
        mTravelRef = mFirebaseDB.getReference(FirebaseContract.TRAVEL);

        mMainContainer = root.findViewById(R.id.frame_layout_travel_fragment_container);
        mMainContainer.setOnRefreshListener(this);

        // add view
        mViewAdapter = new TravelListAdapter(mParentActivity, mUser.getUid());

        RecyclerView.LayoutManager manager = new LinearLayoutManager(mParentActivity, LinearLayoutManager.VERTICAL, false);
        final RecyclerView recyclerView = root.findViewById(R.id.recycler_view_fragment_travel);

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(mViewAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        mViewAdapter.setOnClickListener(new OnTravelRecyclerViewClickListener() {
            @Override
            public void setOnClickListener(TravelListAdapter.TravelRecyclerViewHolder holder, View view, int pos) {
                Travel curTravelItem = mViewAdapter.getTravelItem(pos);
                Intent intent = new Intent(mParentActivity, DetailTravelInfoActivity.class);
                intent.putExtra("travel_info", curTravelItem);
                getActivity().startActivityForResult(intent, MainActivity.TRAVEL_DETAIL_REQUEST);
            }
        });


        final Button orderSelectBtn = root.findViewById(R.id.btn_fragment_travel_order);
        orderSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String items[] = {"최신순", "좋아요순"};
                AlertDialog.Builder ab = new AlertDialog.Builder(mParentActivity);
                ab.setTitle("정렬 순서 정하기");
                currentOrder = 0;
                ab.setSingleChoiceItems(items, 0,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                currentOrder = whichButton;
                            }
                        }).setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                orderSelectBtn.setText(items[currentOrder]);
                                orderSelectBtn.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(sOrderImage[currentOrder]), null, null, null);

                                if(currentOrder == 0) {
                                    query = mTravelRef.orderByChild("writeDate");
                                }
                            }
                        });
                ab.show();
            }
        });

        citySelectBtn = root.findViewById(R.id.btn_fragment_travel_city_select);
        citySelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mParentActivity, CitySelectActivity.class);
                mParentActivity.startActivityForResult(intent, FragmentRouteRecommendation.CITY_SELECT_REQUEST);
            }
        });

        // 아이템 마지막 감지
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int itemTotalCount = recyclerView.getAdapter().getItemCount() - 1;
                if (lastVisibleItemPosition == itemTotalCount) {
                    fetchTravelList();
                    Toast.makeText(getContext(), "Last Position", Toast.LENGTH_SHORT).show();
                }
            }
        });

        query = mTravelRef.orderByChild("writeDate");

        getLatestTravelList(query);

        return root;
    }

    @Override
    public void onRefresh() {
        // 새로 Travel 글 목록 가져오기
        getLatestTravelList(query);
    }

    public void addTravelInfo(final Travel travel) {
        mViewAdapter.addViewItem(travel);
        mViewAdapter.notifyDataSetChanged();

        final Room room = new Room(travel.getContentId(), String.format("%s : %s - %s 여행이야기", travel.getRegion(),
                sSimpleDateFormat.format(travel.getTravelStartDate()),
                sSimpleDateFormat.format(travel.getTravelEndDate())),
                null, new Date(), false, 0);

        final User user = new User(mUser);

        // 채팅방 멤버에 나만 추가하기
        mMemRef.child(travel.getContentId()).child(travel.getWriterUid()).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    mUserRef.child(travel.getWriterUid()).child(FirebaseContract.ROOM).child(travel.getContentId()).setValue(room, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            mUserRef.child(travel.getWriterUid()).child(FirebaseContract.TRAVEL).child(travel.getContentId()).setValue(travel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Snackbar.make(getView(), "방이 만들어 졌습니다. 글 정보 등록", Snackbar.LENGTH_SHORT).show();
                                    onRefresh();
                                }
                            });
                        }
                    });

                } else {
                    Snackbar.make(getView(), "Error", Snackbar.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    // 사용자 목록은 일단 나의 정보만 추가
    public void modifiedTravelInfo(final Travel travel) {

        mUserRef.child(travel.getWriterUid()).child(FirebaseContract.TRAVEL).child(travel.getContentId()).setValue(travel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mViewAdapter.updatePost(travel);
                    mViewAdapter.notifyDataSetChanged();
                    //onRefresh();
                }
            }
        });
    }


    private void fetchTravelList() {
        int preNum = mCurrentPostNum;
        for (int i = 0; i < 5 && mCurrentPostNum < mTotalNum; ++mCurrentPostNum, ++i) {
            DataSnapshot snapshot = travelListSnapshot.next();
            //Travel travel = travelListSnapshot.next().getValue(Travel.class);
            Travel travel = new Travel();

            if(!currentCity.equals("도시 선택") && !currentCity.equals(snapshot.child("region").getValue(String.class))) {
                continue;
            }

            travel.setContent(snapshot.child("content").getValue(String.class));

            if (snapshot.child("photoPath").getValue(String.class) != null)
                travel.setPhotoPath(snapshot.child("photoPath").getValue(String.class));
            travel.setRegion(snapshot.child("region").getValue(String.class));
            travel.setTravelStartDate(snapshot.child("travelStartDate").getValue(Date.class));
            travel.setTravelEndDate(snapshot.child("travelEndDate").getValue(Date.class));
            travel.setWriteDate(snapshot.child("writeDate").getValue(Date.class));
            travel.setWriterUid(snapshot.child("writerUid").getValue(String.class));
            travel.setContentId(snapshot.getKey());
            travel.setPhotoPath(snapshot.child("photoPath").getValue(String.class));

            // ArrayList만 빼오기
            RouteInfo info = new RouteInfo();
            DataSnapshot routeSnapshot = snapshot.child("routeInfo");
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
            travel.setRouteInfo(info);
            mViewAdapter.addViewItem(travel);
        }
        mViewAdapter.notifyDataSetChanged();

        if(mMainContainer.isRefreshing())
            mMainContainer.setRefreshing(false);
        // 다이얼로그 종료
    }

    private void getLatestTravelList(Query query) {
        // 다이얼로그 시작
        mViewAdapter.clear();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // 새로 받은 글 보여주기
                travelListSnapshot = dataSnapshot.getChildren().iterator();
                mCurrentPostNum = 0; // 번호 초기화
                mTotalNum = (int) dataSnapshot.getChildrenCount();
                mViewAdapter.clear();
                fetchTravelList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void removeItem(Travel item) {
        mViewAdapter.removeItem(item);
        //mViewAdapter.notifyDataSetChanged();
        Toast.makeText(mParentActivity, "글을 삭제했습니다", Toast.LENGTH_SHORT).show();
    }

    public void setCurrentCity(String city) {
        currentCity = city;
        citySelectBtn.setText(currentCity);
        getLatestTravelList(query);
    }
}
