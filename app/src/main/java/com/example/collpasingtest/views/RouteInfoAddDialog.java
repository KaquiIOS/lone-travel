package com.example.collpasingtest.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.example.collpasingtest.R;
import com.example.collpasingtest.interfaces.DialogListener;

public class RouteInfoAddDialog extends Dialog implements View.OnClickListener {

    private EditText title, content;
    private String sTitle, sContent;
    private boolean isEnrolled = false;
    private int selectedMode = -1;

    public RouteInfoAddDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_route_info_add);

        title = findViewById(R.id.edit_text_route_info_add_title);
        content = findViewById(R.id.edit_text_route_info_add_content);

        Button btnAdd = this.findViewById(R.id.btn_route_info_dialog_add);
        btnAdd.setOnClickListener(this);

        Button btnCancel = findViewById(R.id.btn_route_info_dialog_cancel);
        btnCancel.setOnClickListener(this);


        final RadioGroup rGroup = findViewById(R.id.rgp_route_info_dialog_option);

        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbtn_route_info_dialog_minimum_cost:
                        selectedMode = 2;
                        break;
                    case R.id.rbtn_route_info_dialog_minimum_walk:
                        selectedMode = 3;
                        break;
                    case R.id.rbtn_route_info_dialog_shortest:
                        selectedMode = 1;
                        break;
                }
            }
        });

        final RadioButton btn = findViewById(R.id.rbtn_route_info_dialog_shortest);

        // viewpager clicklistener
        Switch modeSelector = findViewById(R.id.switch_route_info_dialog_suggestion);
        modeSelector.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    rGroup.setVisibility(View.VISIBLE);
                    btn.setChecked(true);
                    selectedMode = 1;
                }
                else {
                    rGroup.setVisibility(View.GONE);
                    selectedMode = -1;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_route_info_dialog_add:
                isEnrolled = true;
                sTitle = title.getText().toString();
                sContent = content.getText().toString();
                dismiss();
                break;
            case R.id.btn_route_info_dialog_cancel:
                dismiss();
                break;
        }
    }

    public int getSelectedMode() { return selectedMode; }

    public boolean isEnrolled() {
        return isEnrolled;
    }

    public String getsTitle() {
        return sTitle;
    }

    public String getsContent() {
        return sContent;
    }
}
