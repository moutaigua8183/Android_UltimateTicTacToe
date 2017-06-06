package com.moutaigua.ultimatetictactoe;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

/**
 * Created by mou on 3/10/17.
 */

public class ActivityHowToPlay extends Activity {

    private TextView title;
    private TextView rule_introduction;
    private TextView rule_para1;
    private TextView rule_para2;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Typeface titleFont=Typeface.createFromAsset(getAssets(),"fonts/Patchwork_Stitchlings.ttf");
        Typeface dispFont=Typeface.createFromAsset(getAssets(),"fonts/Sansation_Light.ttf");


        title = (TextView) findViewById(R.id.activity_how_to_play_txtview_title);
        title.setTypeface(titleFont);
        rule_introduction = (TextView) findViewById(R.id.activity_how_to_play_txtview_introduction);
        rule_introduction.setTypeface(dispFont);
        rule_para1 = (TextView) findViewById(R.id.activity_how_to_play_txtview_rules_para1);
        rule_para1.setTypeface(dispFont);
        rule_para2 = (TextView) findViewById(R.id.activity_how_to_play_txtview_rules_para2);
        rule_para2.setTypeface(dispFont);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SoundHelper.getInstance(getApplicationContext()).playButtonClick();
    }
}
