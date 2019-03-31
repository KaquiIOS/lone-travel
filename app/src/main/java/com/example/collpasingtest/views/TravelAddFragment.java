package com.example.collpasingtest.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.collpasingtest.R;
import com.example.collpasingtest.interfaces.TravelAddInterface;
import com.example.collpasingtest.models.Route;
import com.example.collpasingtest.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class TravelAddFragment extends Fragment {

    public static final int GET_IMAGE_REQUEST_CODE = 1001,
                             GET_ROUTE_REQUEST_CODE = 1002;

    private EditText mContentEditText;
    private ImageButton mPhotoBtn, mRouteBtn, mRemoveBtn;
    private ImageView mSelectedImageView;
    private ConstraintLayout mSelectedImageContainer;
    private TravelAddActivity mParentActivity;
    private TravelAddInterface mTravelAddInterface;


    private String mSelectedImageUri = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof TravelAddInterface) {
            mParentActivity = (TravelAddActivity) context;
            mTravelAddInterface = (TravelAddInterface) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_add_travel, container, false);

        // 현재 사용자 정보
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // 이전 설정 정보들
        Bundle bundle = getArguments();
        // 클릭 리스너
        TravelAddFragmentOnClickListener listener = new TravelAddFragmentOnClickListener();

        mSelectedImageView = root.findViewById(R.id.image_view_add_travel_selected_image);
        mContentEditText = root.findViewById(R.id.edit_text_add_travel_content);
        mPhotoBtn = root.findViewById(R.id.image_btn_add_travel_photo_select);
        mRouteBtn = root.findViewById(R.id.image_btn_add_travel_route_select);
        mSelectedImageContainer = root.findViewById(R.id.container_tdd_travel_selected_image);
        mRemoveBtn = mSelectedImageContainer.findViewById(R.id.image_btn_add_travel_remove_image_btn);

        mPhotoBtn.setOnClickListener(listener);
        mRouteBtn.setOnClickListener(listener);
        mRemoveBtn.setOnClickListener(listener);

        CircleImageView userProfile = root.findViewById(R.id.image_view_add_travel_profile);
        userProfile.setBackground(new ShapeDrawable(new OvalShape()));

        ImageButton travelAdd  = mParentActivity.findViewById(R.id.toolbar_add_travel_activity).findViewById(R.id.image_btn_add_travel_activity_add);
        travelAdd.setOnClickListener(listener);

        TextView userName = root.findViewById(R.id.text_view_add_travel_user_name),
                 travelPeriod = root.findViewById(R.id.text_view_add_travel_travel_date),
                 travelRegion = root.findViewById(R.id.text_view_add_travel_travel_region);

        Glide.with(root).load(firebaseUser.getPhotoUrl()).into(userProfile); // 이미지 넣기
        userName.setText(firebaseUser.getDisplayName()); // 사용자 이름 넣기
        travelPeriod.setText(bundle.getString(mParentActivity.getString(R.string.add_travel_period))); // 여행 일정 설정
        travelRegion.setText(bundle.getString(mParentActivity.getString(R.string.add_travel_region))); // 여행 지역 설정

        if(bundle.getString("content") != null) {
            mContentEditText.setText(bundle.getString("content"));
        }
        if(bundle.getSerializable("route") != null) {

        }

        if(bundle.getSerializable("prePhoto") != null) {
            mSelectedImageUri = bundle.getSerializable("prePhoto").toString();
            Glide.with(root).load(mSelectedImageUri).into(mSelectedImageView);
            mSelectedImageView.setVisibility(View.VISIBLE);
            mRemoveBtn.setVisibility(View.VISIBLE);
        } else {
        mSelectedImageView.setVisibility(View.GONE);
            mRemoveBtn.setVisibility(View.GONE);
        }

        return root;
    }

    private class TravelAddFragmentOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.image_btn_add_travel_activity_add:
                    if(mContentEditText.getText().toString().isEmpty()) {
                        Snackbar.make(getView(), "내용을 입력해주세요 !", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    // 정보 설정
                    mTravelAddInterface.setContent(mContentEditText.getText().toString(), mSelectedImageUri);
                    break;

                case R.id.image_btn_add_travel_photo_select:
                    getImageUri();
                    break;

                case R.id.image_btn_add_travel_route_select:
                    // 나중에 따로 처리
                    Intent intent = new Intent(mParentActivity, RouteSelectActivity.class);

                    mParentActivity.startActivityForResult(intent, TravelAddActivity.ROUTE_SELECT_REQUEST);
                    break;

                case R.id.image_btn_add_travel_remove_image_btn:
                    removeSelectedImage();
                    break;
            }
        }
    }

    private void getImageUri() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        mParentActivity.startActivityForResult(intent, GET_IMAGE_REQUEST_CODE);
    }

    private void removeSelectedImage() {
        mSelectedImageUri = null;
        mSelectedImageView.setImageDrawable(null);
        mSelectedImageView.setVisibility(View.GONE);
        mRemoveBtn.setVisibility(View.GONE);
    }

    public void setImage(String uri) {
        mSelectedImageUri = uri;
        // imageView set
        mSelectedImageView.setVisibility(View.VISIBLE);
        mRemoveBtn.setVisibility(View.VISIBLE);
        Glide.with(mParentActivity).load(mSelectedImageUri).into(mSelectedImageView);
    }

    public void setPreText(String text) {

    }
}
