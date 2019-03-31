package com.example.collpasingtest.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.collpasingtest.PubPathSuggester;
import com.example.collpasingtest.R;
import com.example.collpasingtest.adapters.AddedRouteAdapter;
import com.example.collpasingtest.adapters.LocationItemAdapter;
import com.example.collpasingtest.adapters.POISearchAdapter;
import com.example.collpasingtest.interfaces.FirebaseContract;
import com.example.collpasingtest.interfaces.LocationItemClickListener;
import com.example.collpasingtest.interfaces.RecyclerViewItemTouchCallback;
import com.example.collpasingtest.interfaces.RecyclerViewOnClickListener;
import com.example.collpasingtest.interfaces.SearchRecyclerViewAdapterCallback;
import com.example.collpasingtest.models.Path;
import com.example.collpasingtest.models.PathType;
import com.example.collpasingtest.models.RouteInfo;
import com.example.collpasingtest.models.SearchInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;
import java.util.Iterator;

public class RouteAddActivity extends AppCompatActivity implements SearchRecyclerViewAdapterCallback, LocationItemClickListener {

    private EditText mSearchEdt;
    private ImageButton mSearchBtn, mShowRouteBtn, mBackBtn;
    private Button mEnroll, mCancel;
    private TMapView mMapView;
    private final long DELAY = 500;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchThread = null;

    private ConstraintLayout mMapContainer, mSearchContainer;

    private ViewPager mSearchListPager;
    private LocationItemAdapter mLocationPagerAdapter;

    private AddedRouteAdapter mRouteAddedAdapter;
    private RecyclerView mRouteAddedRecyclerView;
    private LinearLayout mRouteContainer;

    // pager adapter
    private POISearchAdapter mSearchAdapter;
    private RecyclerView mSearchRecyclerView;

    private RouteAddClickListener clickListener = new RouteAddClickListener();

    private ArrayList<PathType> resultList = new ArrayList<>();

    private RouteInfoAddDialog dialog;
    private String title, content;

    private int selectedMode = -1;

    private boolean isModifyMode = false;
    private boolean isChanged = false;

    private RouteInfo preInfo = null;
    private ArrayList<PathType> prePath = null;
    private InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_add);

        preInfo = (RouteInfo)getIntent().getSerializableExtra("info");
        if(preInfo != null) {
            isModifyMode = true;
            prePath = preInfo.getRouteList();

            Iterator<PathType> it = prePath.iterator();

            while(it.hasNext()) {
                if(it.next() instanceof Path)
                    it.remove();
            }
        }

        dialog = new RouteInfoAddDialog(this);

        initViews();
        setViews();
        setListener();
    }

    private void initViews() {

        mEnroll = findViewById(R.id.btn_route_add_enroll);
        mCancel = findViewById(R.id.btn_route_add_cancel);
        mMapContainer = findViewById(R.id.container_route_add_map);
        mSearchContainer = findViewById(R.id.container_route_add_search);
        mLocationPagerAdapter = new LocationItemAdapter(getSupportFragmentManager());
        mRouteContainer = findViewById(R.id.container_route_add_added_route);

        mRouteAddedRecyclerView = findViewById(R.id.recycler_view_route_add_added_route);
        mRouteAddedAdapter = new AddedRouteAdapter(getApplicationContext());

        mRouteAddedAdapter.setRecyclerViewOnClickListener(new RecyclerViewOnClickListener() {
            @Override
            public void onClick(int position) {
                if(isModifyMode)
                    isChanged = true;
                mRouteAddedAdapter.remove(position);
                mRouteAddedAdapter.notifyDataSetChanged();
            }
        });

        ItemTouchHelper.Callback callback = new RecyclerViewItemTouchCallback(mRouteAddedAdapter);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(mRouteAddedRecyclerView);
        mRouteAddedRecyclerView.setAdapter(mRouteAddedAdapter);
        mRouteAddedRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        if(isModifyMode)
            resultList.addAll(prePath);

        mRouteAddedAdapter.notifyDataSetChanged();

        mSearchAdapter = new POISearchAdapter(this);
        mSearchRecyclerView = mSearchContainer.findViewById(R.id.recycler_view_route_add_search_list);

        mSearchEdt = findViewById(R.id.edit_text_route_add_search);
        mSearchBtn = findViewById(R.id.image_btn_route_add_search);
        mShowRouteBtn = findViewById(R.id.image_btn_route_add_show_route);
        mBackBtn = findViewById(R.id.image_btn_route_add_back);
        mMapView = mMapContainer.findViewById(R.id.tmap_route_add);

        inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);

        mSearchListPager = mMapContainer.findViewById(R.id.view_pager_route_add_location_list);
        mSearchListPager.setAdapter(mLocationPagerAdapter);
    }

    private void setViews() {
        // set app key
        mMapView.setSKTMapApiKey(getString(R.string.app_api_key));

        // viewpager setting
        mSearchAdapter.setCallback(this);
        mSearchRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mSearchRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mSearchRecyclerView.setAdapter(mSearchAdapter);

        // viewpager padding,clip
        mSearchListPager.setClipToPadding(false);
        mSearchListPager.setOffscreenPageLimit(10);
    }

    private void setListener() {
        mSearchBtn.setOnClickListener(clickListener);
        mShowRouteBtn.setOnClickListener(clickListener);
        mBackBtn.setOnClickListener(clickListener);
        mEnroll.setOnClickListener(clickListener);
        mCancel.setOnClickListener(clickListener);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

                // 취소를 누른 경우
                if (!((RouteInfoAddDialog) dialog).isEnrolled()) finish();

                title = ((RouteInfoAddDialog) dialog).getsTitle();
                content = ((RouteInfoAddDialog) dialog).getsContent();

                int type = ((RouteInfoAddDialog) dialog).getSelectedMode();

                final Intent intent = new Intent();
                intent.putExtra("info", resultList);
                intent.putExtra("title", title);
                intent.putExtra("content", content);
                if(preInfo != null)
                    intent.putExtra("key", preInfo.getRouteID());

                if(type == -1) {
                    if(!isModifyMode)
                        setResult(MainActivity.ROUTE_ADD_REQUEST, intent);
                    else {
                        intent.putExtra("isChanged", isChanged);
                        setResult(SelectedRouteActivity.MODIFY_ROUTE_REQUEST, intent);
                    }
                }
                intent.putExtra("searchList", content);
                final PubPathSuggester parser = new PubPathSuggester(getApplicationContext());
                parser.setSearchType(type);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < resultList.size() - 1; ++i) {
                            // 경로 생성
                            if (resultList.get(i) instanceof Path) continue;

                            Path path = parser.getPath(((SearchInfo) resultList.get(i)).getLongitude(), ((SearchInfo) resultList.get(i)).getLatitude(),
                                    ((SearchInfo) resultList.get(i + 1)).getLongitude(), ((SearchInfo) resultList.get(i + 1)).getLatitude());

                            if (path == null) continue;

                            resultList.add(i + 1, path);
                        }

                        intent.putExtra("info", resultList);

                        if(!isModifyMode)
                            setResult(MainActivity.ROUTE_ADD_REQUEST, intent);
                        else {
                            intent.putExtra("isChanged", true);
                            setResult(SelectedRouteActivity.MODIFY_ROUTE_REQUEST, intent);
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        });
                    }
                }).start();
            }
        });

        mSearchListPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageScrollStateChanged(int state) { }

            @Override
            public void onPageSelected(int position) {
                SearchInfo info = (SearchInfo) mLocationPagerAdapter.getItem(position).getArguments().get("info");
                mMapView.setCenterPoint(info.getLongitude(), info.getLatitude());
            }
        });

        mSearchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.length() == 0 || mSearchEdt.getText().toString().equals(s))
                    return;
                mSearchContainer.setVisibility(View.VISIBLE);
                mMapContainer.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                final String input = s.toString();

                // remove previous search thread
                handler.removeCallbacks(searchThread);
                searchThread = new Runnable() {
                    @Override
                    public void run() {
                        mSearchAdapter.filter(input);
                    }
                };
                handler.postDelayed(searchThread, DELAY);
            }
        });
    }

    private class RouteAddClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_route_add_enroll:
                    if(resultList.size() == 0) {
                        Toast.makeText(getApplicationContext(), "경로를 먼저 등록해주세요 !", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    dialog.show();
                    break;

                case R.id.btn_route_add_cancel:
                    finish();
                    break;

                case R.id.image_btn_route_add_search:

                    final String input = mSearchEdt.getText().toString();

                    inputMethodManager.hideSoftInputFromWindow(mSearchEdt.getWindowToken(),0);

                    handler.removeCallbacks(searchThread);
                    searchThread = new Runnable() {
                        @Override
                        public void run() {
                            // 검색 정보 받아오기
                            mSearchAdapter.filter(input);
                            mLocationPagerAdapter.clear();

                            setContainerVisibility(View.GONE, View.VISIBLE, View.VISIBLE, View.GONE);

                            // 밑에 뷰 보이게하기
                            ArrayList<SearchInfo> tmp = mSearchAdapter.getSearchList();

                            if (tmp.isEmpty()) return;

                            int len = tmp.size() > 20 ? 20 : tmp.size();
                            // ViewPager에 등록
                            for (int i = 0; i < len; ++i) {
                                mLocationPagerAdapter.setLocationInfo(tmp.get(i));
                                addMarker(tmp.get(i).getTitle(), new TMapPoint(tmp.get(i).getLatitude(), tmp.get(i).getLongitude()));
                            }
                            mMapView.setCenterPoint(tmp.get(0).getLongitude(), tmp.get(0).getLatitude()); // to first location
                            mLocationPagerAdapter.notifyDataSetChanged();
                        }
                    };
                    handler.postDelayed(searchThread, DELAY);
                    break;

                case R.id.image_btn_route_add_show_route:
                    // result root 보여주기
                    if (mRouteContainer.getVisibility() == View.VISIBLE) {
                        // 이미 목록을 보여주고 있는 경우
                        setContainerVisibility(View.GONE, View.VISIBLE, View.GONE, View.GONE);
                    } else {
                        // 목록을 띄우는 경우
                        mRouteAddedAdapter.setLocations(resultList);
                        mRouteAddedAdapter.notifyDataSetChanged();
                        setContainerVisibility(View.GONE, View.GONE, View.GONE, View.VISIBLE);
                    }
                    break;

                case R.id.image_btn_route_add_back:
                    // back btn 구현
                    onBackPressed();
                    break;
            }
        }
    }

    private void addMarker(String markerName, TMapPoint tMapPoint) {

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background);

        TMapMarkerItem marker = new TMapMarkerItem();

        marker.setIcon(bitmap);
        marker.setID(markerName);
        marker.setTMapPoint(tMapPoint);
        marker.setPosition(0.0f, 1.0f);

        mMapView.addMarkerItem(markerName, marker);
    }

    // 아이템 클릭 => 텍스트 올려주기
    @Override
    public void updateData(String title) {
        mSearchEdt.setText(title);
        mSearchEdt.setSelection(title.length());
        setContainerVisibility(View.GONE, View.VISIBLE, View.GONE, View.GONE);
    }

    @Override
    public void addLocationItem(SearchInfo info) {
        resultList.add(info);
        setContainerVisibility(View.GONE, View.VISIBLE, View.GONE, View.GONE);

        isChanged = true;

        //mSearchEdt.setText("");
        mMapView.removeAllMarkerItem();
        mSearchAdapter.clear();
    }

    @Override
    public void cancelLocationItem() {
        setContainerVisibility(View.GONE, View.VISIBLE, View.GONE, View.GONE);
        //mSearchEdt.setText("");
        mMapView.removeAllMarkerItem();
        mSearchAdapter.clear();
    }

    private void setContainerVisibility(final int searchContainer, final int mapContainer,
                                        final int pager, final int route) {
        mSearchContainer.setVisibility(searchContainer);
        mMapContainer.setVisibility(mapContainer);
        mSearchListPager.setVisibility(pager);
        mRouteContainer.setVisibility(route);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mSearchContainer.getVisibility() == View.VISIBLE) {
            mSearchContainer.setVisibility(View.GONE);
            mMapContainer.setVisibility(View.VISIBLE);
        } else {
            finish();
        }
    }
}
