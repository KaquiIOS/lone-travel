package com.example.collpasingtest.views;

import android.Manifest;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.collpasingtest.MyPermissionChecker;
import com.example.collpasingtest.adapters.MainTabAdapter;
import com.example.collpasingtest.R;
import com.example.collpasingtest.models.PathType;
import com.example.collpasingtest.models.RouteInfo;
import com.example.collpasingtest.models.Travel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final int TRAVEL_UPDATE_CODE = 4111, TRAVEL_DETAIL_REQUEST = 4222, ROUTE_ADD_REQUEST = 4333, SELECTED_ROUTE_REQUEST = 4444;

    private ViewPager mViewPager;
    private MainTabAdapter mTabLayoutAdapter;
    private ImageButton mAddButton;
    private TextView mTabTitleTextview;

    private static final int[] DEFAULT_TAB_IMAGES = {R.drawable.ic_main_activity_tab_travel_default_32, R.drawable.ic_main_activity_tab_chat_default_32,
            R.drawable.ic_main_activity_tab_route_default_32, R.drawable.ic_main_activity_tab_recommendation_default_32, R.drawable.ic_main_activity_tab_profile_default_32};
    private static final int[] SELECTED_TAB_IMAGES = {R.drawable.ic_main_activity_tab_travel_selected_32, R.drawable.ic_main_activity_tab_chat_selected_32,
            R.drawable.ic_main_activity_tab_route_selected_32, R.drawable.ic_main_activity_tab_recommendation_selected_32, R.drawable.ic_main_activity_tab_profile_selected_32};

    private static final int TAB_TRAVEL = 0, TAB_CHAT = 1, TAB_ROUTE = 2, TAB_RECOMMENDATION = 3, TAB_PROFILE = 4;

    private static final int PERMISSION_REQUEST = 1001;

    private static int sCurPos = TAB_TRAVEL;

    private TravelFragment mTravelFragment;
    private FragmentRoute mRouteFragment;
    private ChatListFragment mChatListFragment;
    private FragmentRouteRecommendation mRouteRecommendationFragment;
    private FragmentProfile mFragmentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        new MyPermissionChecker(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST).requestNeededPermission();

        // 툴바 포함 뷰
        mTabTitleTextview = findViewById(R.id.text_view_main_activity_tab_title);
        mAddButton = findViewById(R.id.image_btn_main_activity_add);
        mAddButton.setOnClickListener(new onMainButtonClickListener());

        mTravelFragment = new TravelFragment();
        mRouteFragment = new FragmentRoute();
        mRouteRecommendationFragment = new FragmentRouteRecommendation();
        mChatListFragment = new ChatListFragment();
        mFragmentProfile = new FragmentProfile();


        // tabLayoutAdapter setting
        mTabLayoutAdapter = new MainTabAdapter(getSupportFragmentManager(), this);
        mTabLayoutAdapter.addFragment(mTravelFragment);
        mTabLayoutAdapter.addFragment(mChatListFragment);
        mTabLayoutAdapter.addFragment(mRouteFragment);
        mTabLayoutAdapter.addFragment(mRouteRecommendationFragment);
        mTabLayoutAdapter.addFragment(mFragmentProfile);

        // viewpager setting
        mViewPager = findViewById(R.id.view_pager_main_activity);
        mViewPager.setAdapter(mTabLayoutAdapter);
        mViewPager.setOffscreenPageLimit(5);

        // set TabLayout
        final TabLayout tabLayout = findViewById(R.id.tab_layout_main_activity);
        tabLayout.setupWithViewPager(mViewPager);

        // set default selected tab icon
        tabLayout.getTabAt(TAB_TRAVEL).setIcon(SELECTED_TAB_IMAGES[0]);
        mTabTitleTextview.setText(getString(R.string.travel_toolbar));

        // set tab default icons
        for (int i = 1; i < tabLayout.getTabCount(); i++)
            tabLayout.getTabAt(i).setIcon(DEFAULT_TAB_IMAGES[i]);

        // tab change event handle
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                sCurPos = tab.getPosition();
                tabLayout.getTabAt(sCurPos).setIcon(SELECTED_TAB_IMAGES[sCurPos]);
                // set visibility
                changeToolbarContent();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                tabLayout.getTabAt(pos).setIcon(DEFAULT_TAB_IMAGES[pos]);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }


    private void changeToolbarContent() {

        switch (sCurPos) {
            case TAB_TRAVEL:
                mTabTitleTextview.setText(getString(R.string.travel_toolbar));
                mAddButton.setVisibility(View.VISIBLE);
                break;

            case TAB_CHAT:
                mTabTitleTextview.setText(getString(R.string.chat_toolbar));
                mAddButton.setVisibility(View.GONE);
                break;

            case TAB_ROUTE:
                mTabTitleTextview.setText(getString(R.string.travel_route_toolbar));
                mAddButton.setVisibility(View.VISIBLE);
                break;

            case TAB_RECOMMENDATION:
                mTabTitleTextview.setText(getString(R.string.route_recommendation_toolbar));
                mAddButton.setVisibility(View.GONE);
                break;

            case TAB_PROFILE:
                mTabTitleTextview.setText(getString(R.string.profile_toolbar));
                mAddButton.setVisibility(View.GONE);
                break;
        }
    }


    private class onMainButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.image_btn_main_activity_add:

                    Intent intent = null;

                    switch (sCurPos) {
                        case TAB_TRAVEL:
                            //동행 추가(new Activity)
                            intent = new Intent(MainActivity.this, TravelAddActivity.class);
                            startActivityForResult(intent, TravelFragment.TRAVEL_ADD_REQUEST);
                            break;

                        case TAB_ROUTE:
                            // 루트 추가(new Activity)
                            intent = new Intent(mTabLayoutAdapter.getItem(sCurPos).getContext(), RouteAddActivity.class);
                            startActivityForResult(intent, ROUTE_ADD_REQUEST);
                            break;
                    }
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == TravelFragment.TRAVEL_ADD_REQUEST && data != null) {
            // adapter에 추가히기
            Travel addedContent = (Travel) data.getSerializableExtra("travel_info");
            mTravelFragment.addTravelInfo(addedContent);
        }
        // 새로운 travel 객체 생성
        else if (requestCode == TRAVEL_UPDATE_CODE && data != null) {
            Travel updatedContent = (Travel) data.getSerializableExtra("travel_info");
            mTravelFragment.modifiedTravelInfo(updatedContent);
        } else if (requestCode == TRAVEL_DETAIL_REQUEST) {

            if (data == null) return;

            boolean isDeleted = data.getBooleanExtra("isDeleted", false);
            Travel travel = (Travel) data.getSerializableExtra("travel_info");

            if (isDeleted) {
                mTravelFragment.removeItem(travel);
            } else {
                mTravelFragment.modifiedTravelInfo(travel);
            }
        } else if (requestCode == ChatListFragment.JOIN_ROOM_REQUEST_CODE) {
            // 방에 들어간 경우
            mChatListFragment.setsJoinRoomId();


        } else if (requestCode == ROUTE_ADD_REQUEST) {

            if (data == null) return;

            ArrayList<PathType> info = (ArrayList<PathType>) data.getSerializableExtra("info");
            String content = data.getStringExtra("content");
            String title = data.getStringExtra("title");

            mRouteFragment.addItem(new RouteInfo(info, title, content, "", false));
        }
        // 자신의 경로가 선택된 경우
        else if (requestCode == SELECTED_ROUTE_REQUEST) {

            if(data == null) return;

            // 삭제된 데이터인 경우
            if(data.getBooleanExtra("isDeleted", false)) {
                String key = data.getStringExtra("key");
                mRouteFragment.deleteItem(key);
                return;
            }

            String key = data.getStringExtra("key");
            String title = data.getStringExtra("title");
            String content = data.getStringExtra("content");
            ArrayList<PathType> info = (ArrayList<PathType>) data.getSerializableExtra("list");

            mRouteFragment.updateItem(new RouteInfo(info, title, content, key, false));
        }
        // 선택
        else if(requestCode == SharedRouteDialog.CITY_SELECT_REQEUST) {

            if(data == null) return;

            mRouteFragment.sendSelectedCity(data.getStringExtra("info"));
        }
        else if(requestCode == FragmentRouteRecommendation.CITY_SELECT_REQUEST) {
            if(data == null) return;

            mTravelFragment.setCurrentCity(data.getStringExtra("info"));
        }
    }
}
