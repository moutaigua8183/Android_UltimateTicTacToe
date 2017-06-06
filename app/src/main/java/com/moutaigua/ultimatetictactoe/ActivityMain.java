package com.moutaigua.ultimatetictactoe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.login.LoginManager;

/**
 * Created by mou on 3/3/17.
 */

public class ActivityMain extends Activity {

    private final String LOG_TAG = "ActivityMain";
    private final int REQUEST_SETTING_CODE = 1;
    public static final int RESULT_LOG_OUT = 1;

    private TextView title;
    private Button btnMultiplayer;
    private Button btnOnline;
    private Button btnLeaderboard;
    private Button btnHowToPlay;
    private Button btnSetting;
    private Button btnExit;
    private TextView version;


    private User me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Typeface titleFont=Typeface.createFromAsset(getAssets(),"fonts/Patchwork_Stitchlings.ttf");
        Typeface buttonFont=Typeface.createFromAsset(getAssets(),"fonts/Plastic.ttf");
        me = GlobalData.getInstance().getMe();
        FirebaseDatabaseHelper.getInstance().setOnline(me);
        GlobalData.getInstance().getSetting().loadSetting(this);


        title = (TextView) findViewById(R.id.activity_main_txtview_title);
        title.setTypeface(titleFont);

        btnMultiplayer = (Button) findViewById(R.id.activity_main_btn_multiplayer);
        btnMultiplayer.setTypeface(buttonFont);
        btnMultiplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundHelper.getInstance(getApplicationContext()).playButtonClick();
                Intent intent = new Intent(ActivityMain.this, ActivityMultiplayer.class);
                startActivity(intent);
            }
        });

        btnOnline = (Button) findViewById(R.id.activity_main_btn_online);
        btnOnline.setTypeface(buttonFont);
        btnOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundHelper.getInstance(getApplicationContext()).playButtonClick();
                Intent intent = new Intent(ActivityMain.this, ActivityOnline.class);
                startActivity(intent);
            }
        });

        btnLeaderboard = (Button) findViewById(R.id.activity_main_btn_leaderboard);
        btnLeaderboard.setTypeface(buttonFont);
        btnLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundHelper.getInstance(getApplicationContext()).playButtonClick();
                Intent intent = new Intent(ActivityMain.this, ActivityLeaderboard.class);
                startActivity(intent);
            }
        });

        btnHowToPlay = (Button) findViewById(R.id.activity_main_btn_how_to);
        btnHowToPlay.setTypeface(buttonFont);
        btnHowToPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundHelper.getInstance(getApplicationContext()).playButtonClick();
                Intent intent = new Intent(ActivityMain.this, ActivityHowToPlay.class);
                startActivity(intent);
            }
        });

        btnSetting = (Button) findViewById(R.id.activity_main_btn_setting);
        btnSetting.setTypeface(buttonFont);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundHelper.getInstance(getApplicationContext()).playButtonClick();
                Intent intent = new Intent(ActivityMain.this, ActivitySetting.class);
                startActivityForResult(intent, REQUEST_SETTING_CODE);
            }
        });

        btnExit = (Button) findViewById(R.id.activity_main_btn_exit);
        btnExit.setTypeface(buttonFont);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundHelper.getInstance(getApplicationContext()).playButtonClick();
                finish();
            }
        });

        version = (TextView) findViewById(R.id.activity_main_txtview_version);
        version.setText("Version "+UpdateHelper.VERSION);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( resultCode==RESULT_LOG_OUT ){
            logout();
            Intent intent = new Intent(this, ActivityLogin.class);
            startActivity(intent);
            finish();
        }
    }


    @Override
    public void onBackPressed() {
        SoundHelper.getInstance(getApplicationContext()).playButtonClick();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseDatabaseHelper.getInstance().setOffline(me);
    }




    private void logout(){
        if( GlobalData.getInstance().isLoginByEmail() ){
            SharedPreferences.Editor editor = getSharedPreferences(ActivityLogin.SHARED_PREF_EMAIL_PASSWORD, Context.MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();
        } else {
            LoginManager.getInstance().logOut();
        }
        FirebaseAuthHelper.getInstance(this).signOut();
    }


}
