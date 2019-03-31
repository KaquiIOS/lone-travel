package com.example.collpasingtest.views;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.collpasingtest.R;
import com.example.collpasingtest.adapters.CityListAdapter;
import com.example.collpasingtest.adapters.TagAdapter;
import com.example.collpasingtest.interfaces.RecyclerViewOnClickListener;

import java.util.ArrayList;

public class SharedRouteDialog extends Dialog {

    public static final int CITY_SELECT_REQEUST = 7111;

    private Context context;
    private EditText mTitleEdt, mContentEdt, mTagEdt;
    private RecyclerView mTagRecyclerView;
    private TagAdapter tagAdapter;

    private String selectedCity = "", title = "", content = "";
    private Button mCityBtn;
    private boolean isEnrolled = false;

    public SharedRouteDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_shared_route);

        initView();
    }

    private void initView() {

        tagAdapter = new TagAdapter(getContext());
        tagAdapter.setClickListener(new RecyclerViewOnClickListener() {
            @Override
            public void onClick(int position) {
                tagAdapter.removeTag(position);
                tagAdapter.notifyDataSetChanged();
            }
        });

        mTagRecyclerView = findViewById(R.id.recycler_view_shared_dialog_added_tag);
        mTagRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        mTagRecyclerView.setAdapter(tagAdapter);

        mCityBtn = findViewById(R.id.btn_shared_dialog_select_city);
        mCityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent((Activity)context, CitySelectActivity.class);
                ((Activity)context).startActivityForResult(intent, CITY_SELECT_REQEUST);
                ((Activity)context).overridePendingTransition(R.anim.activity_bottom_up, R.anim.activity_slide_out);
            }
        });

        Button mShareBtn = findViewById(R.id.btn_shared_dialog_share);
        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTitleEdt.getText().toString().isEmpty()) {
                    mTitleEdt.setHintTextColor(context.getColor(R.color.main_red));
                    Toast.makeText(context, "제목을 입력해주세요 !", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(mContentEdt.getText().toString().isEmpty()) {
                    mContentEdt.setHintTextColor(context.getColor(R.color.main_red));
                    Toast.makeText(context, "내용을 입력해주세요 !", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(selectedCity.isEmpty()) {
                    mCityBtn.setTextColor(context.getColor(R.color.main_red));
                    Toast.makeText(context, "여행 도시를 선택해주세요 !", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(tagAdapter.getItemCount() == 0) {
                    mTitleEdt.setHintTextColor(context.getColor(R.color.main_red));
                    Toast.makeText(context, "태그를 1개 이상 입력해주세요 !", Toast.LENGTH_SHORT).show();
                    return;
                }

                isEnrolled = true;
                dismiss();
            }
        });

        Button mCancelBtn = findViewById(R.id.btn_shared_dialog_cancel);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mTitleEdt = findViewById(R.id.edit_text_shared_dialog_title);
        mContentEdt = findViewById(R.id.edit_text_shared_dialog_content);
        mTagEdt = findViewById(R.id.edit_text_shared_dialog_tag);
        mTagEdt.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for(int i = start; i < end; ++i) {
                    if(Character.isSpaceChar(source.charAt(i))) {
                        pushTag(mTagEdt.getText().toString());
                        return null;
                    }
                }
                return source;
            }
        }});
    }

    public void pushTag(String tag) {
        mTagEdt.setText("");
        tagAdapter.addTag(tag);
        tagAdapter.notifyDataSetChanged();
    }

    public boolean isEnrolled() {
        return isEnrolled;
    }

    public String getTitle() {
        return mTitleEdt.getText().toString();
    }

    public String getContent() {
        return mContentEdt.getText().toString();
    }

    public ArrayList<String> getTags() {
        return tagAdapter.getTagList();
    }

    public void setSelectedCity(String selectedCity) {
        this.selectedCity = selectedCity;
        this.mCityBtn.setText(selectedCity);
        this.mCityBtn.setTextColor(context.getColor(R.color.black));
    }

    public String getSelectedCity() {
        return selectedCity;
    }
}
