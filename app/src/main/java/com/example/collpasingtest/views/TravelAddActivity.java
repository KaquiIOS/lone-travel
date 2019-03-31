package com.example.collpasingtest.views;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import com.example.collpasingtest.interfaces.FirebaseContract;
import com.example.collpasingtest.R;
import com.example.collpasingtest.interfaces.TravelAddInterface;
import com.example.collpasingtest.models.Comment;
import com.example.collpasingtest.models.Route;
import com.example.collpasingtest.models.RouteInfo;
import com.example.collpasingtest.models.Travel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TravelAddActivity extends AppCompatActivity implements TravelAddInterface {

    public static final String[] sCities = {
            "seoul", "busan", "daegu", "incheon", "kwangju", "daejeon", "ulsan", "gyeonggi",
            "kangwon", "chungbuk", "chungnam", "jeonbuk", "jeonnam", "gyeongbuk", "gyeongnam", "jeju", "sejong"
    };

    public static final int[] sImageList = {
            R.drawable.img_seoul, R.drawable.img_busan, R.drawable.img_daegu, R.drawable.img_incheon, R.drawable.img_kwangju, R.drawable.img_daegeon,
            R.drawable.img_ulsan, R.drawable.img_kyueonggi, R.drawable.img_kangrueong, R.drawable.img_cheongju, R.drawable.img_kwangju, R.drawable.img_jeonbuk,
            R.drawable.img_jeonnam, R.drawable.img_kyueongbuk, R.drawable.img_kyueongnam, R.drawable.img_jeju, R.drawable.img_sejong
    };

    public static int ROUTE_SELECT_REQUEST = 6111;


    public static final int SELECT_CITY = 1, SELECT_TIME = 2, ADD_TRAVEL = 3;
    private static SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private FragmentManager fm;

    private ImageButton mToolbarBackCancelBtn;
    private ImageButton mToolbarSaveBtn;

    private int mCurPage = SELECT_CITY;

    private String selectedRegion, content;
    private Date startDate, endDate;
    private RouteInfo selectedRoute;
    private String photoPath;

    private CitySelectFragment mCitySelectFragment;
    private TimeSelectFragment mTimeSelectFragment;
    private TravelAddFragment mTravelAddFragment;

    private TravelAddOnClickListener mTravelAddOnClickListener = new TravelAddOnClickListener();

    private FirebaseDatabase mFirebaseDB;
    private String mUid, prePhotoUri = null;
    private DatabaseReference mTravelRef;
    private StorageReference mImageStorageRef;
    private DatabaseReference mRouteRef;

    Travel prePost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_travel);

        // firebase auth
        mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mFirebaseDB = FirebaseDatabase.getInstance();
        mTravelRef = mFirebaseDB.getReference(FirebaseContract.TRAVEL);
        mImageStorageRef = FirebaseStorage.getInstance().getReference(FirebaseContract.TRAVEL);
        mRouteRef = mFirebaseDB.getReference("route");

        mCitySelectFragment = new CitySelectFragment();
        mTimeSelectFragment = new TimeSelectFragment();
        mTravelAddFragment = new TravelAddFragment();


        prePost = (Travel) getIntent().getSerializableExtra("travel_info");

        if (prePost != null) {
            prePhotoUri = prePost.getPhotoPath();
        }

        // get FragmentManager
        fm = getSupportFragmentManager();

        Toolbar toolbar = findViewById(R.id.toolbar_add_travel_activity);
        //LinearLayout container = findViewById(R.id.container_add_travel_activity);

        // View add
        mToolbarBackCancelBtn = toolbar.findViewById(R.id.image_btn_add_travel_activity_cancel_back);
        mToolbarSaveBtn = toolbar.findViewById(R.id.image_btn_add_travel_activity_add);

        // add on clicklistener
        mToolbarSaveBtn.setOnClickListener(mTravelAddOnClickListener);
        mToolbarBackCancelBtn.setOnClickListener(mTravelAddOnClickListener);

        // 첫 프레그먼트 설정
        fm.beginTransaction().replace(R.id.container_add_travel_activity, mCitySelectFragment).commit();
    }

    private int getCityNum(String cityName) {
        int pos = 0;
        for (String city : sCities) {
            if (city.equals(cityName))
                break;
            pos++;
        }
        return pos;
    }


    @Override
    public void setCity(int cityNum) {
        selectedRegion = sCities[cityNum];
        mCurPage = SELECT_TIME;
        mToolbarSaveBtn.setVisibility(View.VISIBLE);

        Bundle bundle = new Bundle();  // Bundle
        bundle.putInt(getString(R.string.add_travel_city_num), cityNum);

        if (prePost != null) {
            bundle.putSerializable("start", prePost.getTravelStartDate());
            bundle.putSerializable("end", prePost.getTravelEndDate());
        }

        mTimeSelectFragment.setArguments(bundle);

        fm.beginTransaction().replace(R.id.container_add_travel_activity, mTimeSelectFragment).addToBackStack(null).commit(); // change fragment
    }

    @Override
    public void setDateInfo(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        mCurPage = ADD_TRAVEL;

        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.add_travel_region), selectedRegion);
        bundle.putString(getString(R.string.add_travel_period), sSimpleDateFormat.format(startDate.getTime()) + "~" + sSimpleDateFormat.format(endDate.getTime()));

        if(prePost != null) {
            bundle.putString("prePhoto", prePhotoUri);
            bundle.putString("content", prePost.getContent());
            bundle.putSerializable("route", prePost.getRouteInfo());
        }

        mTravelAddFragment.setArguments(bundle);

        fm.beginTransaction().replace(R.id.container_add_travel_activity, mTravelAddFragment).addToBackStack(null).commit();
    }

    @Override
    public void setContent(String content, String photoPath) {
        this.content = content;
        this.photoPath = photoPath;
        if (getIntent().getSerializableExtra("travel_info") == null)
            addTravel();
        else
            updateTravel();
    }

    private void updateTravel() {
        final Travel travel = (Travel) getIntent().getSerializableExtra("travel_info");

        travel.setWriteDate(new Date());
        travel.setContent(content);
        travel.setRegion(selectedRegion);
        travel.setPhotoPath(travel.getPhotoPath());
        travel.setTravelStartDate(startDate);
        travel.setTravelEndDate(endDate);
        travel.setRouteInfo(selectedRoute);

        if(photoPath == null) {
            travel.setPhotoPath(null);
            mTravelRef.child(travel.getContentId()).setValue(travel).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent();
                        intent.putExtra("travel_info", travel);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            });
        }
        // 이전에 사용하던 이미지 그대로 사용
        else if (photoPath.equals(travel.getPhotoPath())) {
            // 수정작업
            mTravelRef.child(travel.getContentId()).setValue(travel).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent();
                        intent.putExtra("travel_info", travel);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            });
        } else {
            // 새로운 이미지 등록
            mImageStorageRef.putFile(Uri.parse(photoPath)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        // 이미지 경로 가져오기
                        mImageStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                travel.setPhotoPath(uri.toString());
                                // 이미지 등록 성공 후 원래 액티비티로 돌아가기
                                mTravelRef.child(travel.getContentId()).setValue(travel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Intent intent = new Intent();
                                            intent.putExtra("travel_info", travel);
                                            setResult(RESULT_OK, intent);
                                            finish();
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            });
        }
    }

    private void addTravel() {

        // 새로운 키 받아오기
        final String contentId = mTravelRef.push().getKey();

        final Intent intent = new Intent();

        /*if(selectedRoute != null)
            mRouteRef.child(contentId).setValue(route);*/

        // 이미지 저장해두기
        if (photoPath != null) {
            mImageStorageRef.putFile(Uri.parse(photoPath)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        // 이미지 경로 가져오기
                        mImageStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final Travel travel = new Travel(contentId, mUid, new Date(), content, selectedRegion, uri.toString(),
                                        startDate, endDate, selectedRoute);
                                // 이미지 등록 성공 후 원래 액티비티로 돌아가기
                                mTravelRef.child(contentId).setValue(travel).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            intent.putExtra("travel_info", travel);
                                            setResult(RESULT_OK, intent);
                                            finish();
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            });
        } else {

            final Travel travel = new Travel(contentId, mUid, new Date(), content,
                    selectedRegion, prePhotoUri, startDate, endDate, selectedRoute);

            mTravelRef.child(contentId).setValue(travel).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        intent.putExtra("travel_info", travel);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            });
        }
    }

    private class TravelAddOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.image_btn_add_travel_activity_cancel_back:
                    // 뒤로가기 혹은 취소 버튼
                    switch (mCurPage) {
                        case SELECT_CITY:
                            // 취소 다이얼로그 보여주기
                            setResult(RESULT_CANCELED);
                            finish();
                            break;

                        case SELECT_TIME:
                            mCurPage = SELECT_CITY;
                            fm.beginTransaction().addToBackStack(null).replace(R.id.container_add_travel_activity, mCitySelectFragment).commit();
                            mToolbarSaveBtn.setVisibility(View.GONE);
                            break;

                        case ADD_TRAVEL:
                            mCurPage = SELECT_TIME;
                            fm.beginTransaction().addToBackStack(null).replace(R.id.container_add_travel_activity, mTimeSelectFragment).commit();
                            break;
                    }
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ROUTE_SELECT_REQUEST) {
            if(data == null) {
                selectedRoute = null;
                return;
            }
            selectedRoute = (RouteInfo) data.getSerializableExtra("route");
        }
        else if(requestCode == TravelAddFragment.GET_IMAGE_REQUEST_CODE) {
            if(data == null) return;
            mTravelAddFragment.setImage(data.getData().toString());
        }
    }
}
