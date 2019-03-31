package com.example.collpasingtest.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.collpasingtest.R;
import com.example.collpasingtest.adapters.AddedRouteAdapter;
import com.example.collpasingtest.interfaces.FirebaseContract;
import com.example.collpasingtest.models.Path;
import com.example.collpasingtest.models.PathInfo;
import com.example.collpasingtest.models.PathType;
import com.example.collpasingtest.models.RouteInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SelectedRouteActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int MODIFY_ROUTE_REQUEST = 5111;

    private RecyclerView recyclerView;
    private AddedRouteAdapter adapter;
    private TextView titleView;

    private String title, content;
    private RouteInfo routeInfo;

    private ArrayList<PathType> pathList;

    private Handler handler = new Handler();

    private DatabaseReference mRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_route);

        recyclerView = findViewById(R.id.recycler_view_selected_route_items);

        routeInfo = (RouteInfo)getIntent().getSerializableExtra("info");

        pathList = routeInfo.getRouteList();
        title = routeInfo.getTitle();
        content = routeInfo.getContent();

        mRoute = FirebaseDatabase.getInstance().getReference(FirebaseContract.USERS)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(FirebaseContract.ROUTE);

        initView();
    }


    private void initView() {

        Toolbar toolbar = findViewById(R.id.toolbar_selected_route);
        titleView = toolbar.findViewById(R.id.text_view_selected_route_title);
        titleView.setText(title);

        ImageButton modifyBtn = toolbar.findViewById(R.id.image_btn_selected_route_modify);
        modifyBtn.setOnClickListener(this);

        ImageButton removeBtn = toolbar.findViewById(R.id.image_btn_selected_route_remove);
        removeBtn.setOnClickListener(this);

        recyclerView = findViewById(R.id.recycler_view_selected_route_items);

        adapter = new AddedRouteAdapter(this);
        adapter.setLocations(pathList);
        adapter.setCanRemove(false);
        adapter.setCanMove(false);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.image_btn_selected_route_modify:
                modifyRoute();
                break;
            case R.id.image_btn_selected_route_remove:
                removeRoute();
                break;
        }
    }

    private void modifyRoute() {
        Intent intent = new Intent(SelectedRouteActivity.this, RouteAddActivity.class);
        intent.putExtra("info", routeInfo);
        startActivityForResult(intent, MODIFY_ROUTE_REQUEST);
    }

    private void removeRoute() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("경로 삭제");
        builder.setMessage("나만의 경로를 삭제하시겠습니까 ?");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 삭제로직 수행
                // 삭제된 객체를
                // 저장될 데이터 모아두기
                Intent intent = new Intent();
                intent.putExtra("isDeleted", true);
                intent.putExtra("key", routeInfo.getRouteID());
                setResult(MainActivity.SELECTED_ROUTE_REQUEST, intent);
                finish();
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == MODIFY_ROUTE_REQUEST) {

            if(data == null) return;

            boolean isDataChanged = data.getBooleanExtra("isChanged", false);

            // 데이터가 변경되었으면 현재 리스트 반환 및 서버에 저장
            if(isDataChanged) {

                title = data.getStringExtra("title");
                content = data.getStringExtra("content");
                String key = data.getStringExtra("key");
                pathList.clear();
                pathList.addAll((ArrayList<PathType>)data.getSerializableExtra("info"));

                mRoute.child(key).setValue(new RouteInfo(pathList, title, content, key, false)).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            adapter.setLocations(pathList);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        // 저장될 데이터 모아두기
        Intent intent = new Intent();

        intent.putExtra("key", routeInfo.getRouteID());
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("list", routeInfo.getRouteList());

        setResult(MainActivity.SELECTED_ROUTE_REQUEST, intent);
        finish();
    }
}
