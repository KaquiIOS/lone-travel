package com.example.collpasingtest.views;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.collpasingtest.R;
import com.example.collpasingtest.interfaces.TravelAddInterface;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeSelectFragment extends Fragment {

    private static final SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private TravelAddActivity mParentActivity; // 툴바에서 버튼 이벤트 등록
    private TravelAddInterface mTravelAddInterface;
    private DatePicker mStartDatePicker, mEndDatePicker; // 시작 종료
    private TextView mTotalPeriodTextView, mStartDateTextView, mEndDateTextView; // 시간 변경 이벤트
    private ImageButton mSaveDateInfoBtn;   // Save 이벤트 처리
    private Calendar mStartDate, mEndDate;
    private String sDate, eDate;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TravelAddActivity) {
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
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_time_select, container, false);

        int selectedCityNum = getArguments().getInt(mParentActivity.getString(R.string.add_travel_city_num));

        // 기본 이미지 설정
        ImageView mCityImageView = root.findViewById(R.id.image_view_time_select_city);
        mCityImageView.setImageResource(TravelAddActivity.sImageList[selectedCityNum]);

        // 기간 선택
        mStartDatePicker = root.findViewById(R.id.date_picker_time_select_start_date);
        mEndDatePicker = root.findViewById(R.id.date_picker_time_select_end_date);

        // 기간 텍스트
        mTotalPeriodTextView = root.findViewById(R.id.text_view_time_select_date);

        mStartDateTextView = root.findViewById(R.id.text_view_time_select_start_date);
        mEndDateTextView = root.findViewById(R.id.text_view_time_select_end_date);

        // 저장 버튼 등록
        mSaveDateInfoBtn = mParentActivity.findViewById(R.id.toolbar_add_travel_activity).findViewById(R.id.image_btn_add_travel_activity_add);

        setDefaultInfo();

        return root;
    }

    private void setDefaultInfo() {
        // 디폴트 날
        Calendar startCalendar = Calendar.getInstance();

        if (getArguments().getSerializable("start") != null) {
            startCalendar.setTime((Date)getArguments().getSerializable("start"));
        }

        mStartDate = startCalendar;

        // 변경 이벤트 처리
        mStartDatePicker.init(startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mStartDate.set(year, monthOfYear, dayOfMonth);
                sDate = String.format("%d-%d-%d", year, monthOfYear + 1, dayOfMonth);
                mTotalPeriodTextView.setText(sDate + "~" + eDate);
                mStartDateTextView.setText(sDate);
            }
        });


        Calendar endCalendar = Calendar.getInstance();

        if (getArguments().getSerializable("end") != null) {
            endCalendar.setTime((Date)getArguments().getSerializable("end"));
        }

        mEndDate = endCalendar;

        mEndDatePicker.init(endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mEndDate.set(year, monthOfYear, dayOfMonth);
                eDate = String.format("%d-%d-%d", year, monthOfYear + 1, dayOfMonth);
                mTotalPeriodTextView.setText(sDate + "~" + eDate);
                mEndDateTextView.setText(eDate);
            }
        });

        sDate = sSimpleDateFormat.format(mStartDate.getTime());
        eDate = sSimpleDateFormat.format(mEndDate.getTime());

        mTotalPeriodTextView.setText(sDate + "~" + eDate);
        mStartDateTextView.setText(sDate);
        mEndDateTextView.setText(eDate);

        mSaveDateInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // mStartDate, mEndDate 비교해서 더 작으면 SnackBar 로 오류 알려주기
                if (mEndDate.compareTo(mStartDate) > 0) {
                    mTravelAddInterface.setDateInfo(mStartDate.getTime(), mEndDate.getTime());
                } else {
                    Snackbar.make(getView(), "출발일, 도착일을 확인해 주세요 !", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setPreDate(Date start, Date end) {

    }
}
