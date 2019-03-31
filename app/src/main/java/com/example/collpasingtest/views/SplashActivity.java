package com.example.collpasingtest.views;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.example.collpasingtest.R;
import com.example.collpasingtest.views.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.security.MessageDigest;

public class SplashActivity extends AppCompatActivity {

    public static final int DELAY_TIME = 1000;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // [ Post MainActivity Start ]
        // open Activity after 1 sec
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // open MainActivity
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);

                // End Splash Activity
                finish();
            }
        }, DELAY_TIME);
        // [ Post MainActivity End ]
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
