package com.moutaigua.ultimatetictactoe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.login.LoginManager;


/**
 * Created by mou on 3/10/17.
 */

public class ActivitySetting extends Activity {

    private TextView title;
    private TextView txtInvitable;
    private TextView txtSoundEffect;
    private ToggleButton toggleInvitable;
    private ToggleButton toggleSoundEffect;
    private Button btnFeedback;
    private Button btnLogOut;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Typeface titleFont=Typeface.createFromAsset(getAssets(),"fonts/Patchwork_Stitchlings.ttf");
        Typeface dispFont=Typeface.createFromAsset(getAssets(),"fonts/Sansation_Light.ttf");


        title = (TextView) findViewById(R.id.activity_setting_txtview_title);
        title.setTypeface(titleFont);
        txtInvitable = (TextView) findViewById(R.id.activity_setting_txtview_item_invitable);
        txtInvitable.setTypeface(dispFont);
        txtSoundEffect = (TextView) findViewById(R.id.activity_setting_txtview_item_sound_effect);
        txtSoundEffect.setTypeface(dispFont);
        toggleInvitable = (ToggleButton) findViewById(R.id.activity_setting_toggle_invitable);
        toggleInvitable.setChecked(GlobalData.getInstance().getSetting().isInvitationAllowed());
        toggleInvitable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( toggleInvitable.isChecked() ){
                    SoundHelper.getInstance(getApplicationContext()).playToggleClickOn();
                } else {
                    SoundHelper.getInstance(getApplicationContext()).playToggleClickOff();
                }
            }
        });
        toggleSoundEffect = (ToggleButton) findViewById(R.id.activity_setting_toggle_sound_effect);
        toggleSoundEffect.setChecked(GlobalData.getInstance().getSetting().isSoundEffectAllowed());
        toggleSoundEffect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( toggleSoundEffect.isChecked() ){
                    SoundHelper.getInstance(getApplicationContext()).playToggleClickOn();
                    GlobalData.getInstance().getSetting().setSoundEffectAllowed(true);
                } else {
                    SoundHelper.getInstance(getApplicationContext()).playToggleClickOff();
                    GlobalData.getInstance().getSetting().setSoundEffectAllowed(false);
                }
            }
        });


        btnFeedback = (Button) findViewById(R.id.activity_setting_btn_feedback);
        btnFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundHelper.getInstance(getApplicationContext()).playButtonClick();
                Intent i = new Intent(Intent.ACTION_SENDTO);
                String uriText = "mailto:" + Uri.encode("moutaigua8183@gmail.com") +
                        "?subject=" + Uri.encode("Feedback for Ultimate Tic Tac Toe") +
                        "&body=" + Uri.encode("Hi Mou,");
                Uri uri = Uri.parse(uriText);
                i.setData(uri);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(Intent.createChooser(i, "Send Email Via"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(ActivitySetting.this, "There are no email clients installed.", Toast.LENGTH_LONG).show();
                }
            }
        });
        btnLogOut = (Button) findViewById(R.id.activity_setting_btn_log_out);
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundHelper.getInstance(getApplicationContext()).playButtonClick();
                setResult(ActivityMain.RESULT_LOG_OUT);
                finish();
            }
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SoundHelper.getInstance(getApplicationContext()).playButtonClick();
        boolean invitable = toggleInvitable.isChecked();
        GlobalData.getInstance().getSetting().setInvitationAllowed(invitable);
        GlobalData.getInstance().getMe().setInvitable(invitable);
        FirebaseDatabaseHelper.getInstance().setMyInvitable(invitable);
        boolean sound_effect = toggleSoundEffect.isChecked();
        GlobalData.getInstance().getSetting().setSoundEffectAllowed(sound_effect);

        GlobalData.getInstance().getSetting().saveSetting(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
