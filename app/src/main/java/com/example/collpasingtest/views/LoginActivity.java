package com.example.collpasingtest.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.collpasingtest.PrefManager;
import com.example.collpasingtest.R;
import com.example.collpasingtest.models.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.Login;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final int GOOGLE_SIGN_IN_REQ = 100;
    private static final String TAG = "MAIN_ACTIVITY";
    private static final String FIREBASE_USER_TABLE = "users";

    private SignInButton mGoogleSignInBtn;
    private LoginButton mFaceBookSignInBtn;
    private ProgressBar mProgressBar;

    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInOptions mGoogleSignInOptions;

    private FirebaseAuth mAuth;
    private FirebaseAnalytics mAnalytics;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserRef;

    private CallbackManager mCallbackManager; // 페이스북 로그인 콜백

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnSkip, btnNext;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this); // Facebook Init
        setContentView(R.layout.activity_login);

        mGoogleSignInBtn = findViewById(R.id.btn_main_activity_sign_in);
        mFaceBookSignInBtn = findViewById(R.id.btn_main_activity_facebook_sign_in);
        mFaceBookSignInBtn.setReadPermissions(Arrays.asList("email", "public_profile"));
        mProgressBar = findViewById(R.id.progress_bar_main_activity_login);


        // 파이어베이스 인증
        mAuth = FirebaseAuth.getInstance();

        // DB 레퍼런스
        mDatabase = FirebaseDatabase.getInstance();
        mAnalytics = FirebaseAnalytics.getInstance(this);
        mUserRef = mDatabase.getReference(FIREBASE_USER_TABLE);

        LoginEventHandler loginEventHandler = new LoginEventHandler();

        mGoogleSignInBtn.setOnClickListener(loginEventHandler);
        mFaceBookSignInBtn.setOnClickListener(loginEventHandler);

        // auto login
        if (mAuth.getCurrentUser() != null) {
            showMainActivity();
        }

        prefManager = new PrefManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        viewPager = findViewById(R.id.view_pager);
        dotsLayout = findViewById(R.id.layoutDots);
        btnSkip = findViewById(R.id.btn_skip);
        btnNext = findViewById(R.id.btn_next);


        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{
                R.layout.intro_slide_1,
                R.layout.intro_slide_2,
                R.layout.intro_slide_3,
                R.layout.intro_slide_4};

        // adding bottom dots
        addBottomDots(0);

        // making notification bar transparent
        changeStatusBarColor();

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchHomeScreen();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if last page home screen will be launched
                int current = getItem(+1);
                if (current < layouts.length) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {
                    //launchHomeScreen();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN_REQ) {

            // Login Result Get
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Login Fail Handle
            }
        } else {
            // FaceLogin 의 경우 handle 따로.
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    /* -------------------------------------------- Google Login Event ----------------------------------------------------------- */

    private void setUpGoogleLogin() {

        // Login Option
        mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Login Client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {  /*Login 할 때 UI 자동 관리*/
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                .build();
    }

    private void googleLogin() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, GOOGLE_SIGN_IN_REQ);
    }

    // Login Handle
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        // Log
        Log.d(TAG, "FirebaseAuthWithGoogle : " + account.getId());

        // 사용자 토큰을 이용해 인증 확인
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    // 인증이 완료되었을 때 Handle
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // isComplete : Auth task end
                        // isSuccess  : Authorization success
                        if (task.isComplete()) {
                            // success auth
                            if (task.isSuccessful()) {
                                EnrolledUserInfo(task);
                            } else {
                                Snackbar.make(mProgressBar, "Login Fail", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }


    /* -------------------------------------------- Facebook Login Event ----------------------------------------------------------- */

    private void tryFacebookLogin() {
        mCallbackManager = CallbackManager.Factory.create();

        // Login Result Handle
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // Handle Success Login
                handleFacebookLoginWithToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                // Cancel does not condsier;
            }

            @Override
            public void onError(FacebookException error) {
                // facebook log event
            }
        });
    }


    private void handleFacebookLoginWithToken(AccessToken accessToken) {
        // 로그인 토근 받아오기
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());

        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    EnrolledUserInfo(task);
                } else {
                    // Error
                }
            }
        });
    }


    private void EnrolledUserInfo(@NonNull Task<AuthResult> task) {
        FirebaseUser firebaseUser = task.getResult().getUser();
        final User user = new User(firebaseUser);

        // 데이터베이스 구조가 user/{userid}/something 라서 child 호출
        mUserRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null) {
                    dataSnapshot.getRef().setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                showMainActivity();
                            }
                        }
                    });
                } else {
                    showMainActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void showMainActivity() {
        // to next activity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    private class LoginEventHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_main_activity_sign_in:
                    setUpGoogleLogin();
                    googleLogin();
                    break;

                case R.id.btn_main_activity_facebook_sign_in:
                    tryFacebookLogin();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                btnNext.setVisibility(View.GONE);
                btnSkip.setVisibility(View.GONE);
                mGoogleSignInBtn.setVisibility(View.VISIBLE);
                mFaceBookSignInBtn.setVisibility(View.VISIBLE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
                btnNext.setVisibility(View.VISIBLE);
                btnSkip.setVisibility(View.GONE);

                mGoogleSignInBtn.setVisibility(View.GONE);
                mFaceBookSignInBtn.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
