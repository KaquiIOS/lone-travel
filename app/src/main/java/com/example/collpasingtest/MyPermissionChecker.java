package com.example.collpasingtest;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.Iterator;

public class MyPermissionChecker {


    private ArrayList<String> permissionList = null;
    private Activity activity;
    private int REQUEST_ID;

    public MyPermissionChecker(Activity _activity, ArrayList<String> _permissionList, int _REQUEST_ID) {
        activity = _activity;
        permissionList = (ArrayList<String>)_permissionList.clone();
        REQUEST_ID = _REQUEST_ID;
    }

    public MyPermissionChecker(Activity _activity, String[] _permissions, int _REQUEST_ID) {
        activity = _activity;
        permissionList = new ArrayList<>();
        for (int i = 0; i < _permissions.length; ++i)
            permissionList.add(_permissions[i]);
        REQUEST_ID = _REQUEST_ID;
    }

    public void requestNeededPermission() {

        // Android 6.0 이전에는 따로 받을필요 없음
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;

        Iterator<String> it = permissionList.iterator();

        // 허가 받은 permission 들은 지우기
        while (it.hasNext()) {

            String permission = it.next();

            int hasPerm = activity.checkSelfPermission(permission);

            if (hasPerm == PackageManager.PERMISSION_GRANTED) it.remove();
            // 권한에 대한 설명을 띄워줌 (이미 한 번 거절한 적이 있는 권한들만)
            //else if(activity.shouldShowRequestPermissionRationale(permission)) {}
        }

        // 권한이 없는 것들만 모아서 요구
        String[] arr = new String[permissionList.size()];

        for(int i = 0; i < arr.length; ++i)
            arr[i] = permissionList.get(i);

        if(arr.length != 0)
            activity.requestPermissions(arr, REQUEST_ID);
    }
}
