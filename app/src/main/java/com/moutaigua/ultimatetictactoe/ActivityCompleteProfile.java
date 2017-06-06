package com.moutaigua.ultimatetictactoe;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mukesh.countrypicker.fragments.CountryPicker;
import com.mukesh.countrypicker.interfaces.CountryPickerListener;

/**
 * Created by mou on 3/9/17.
 */

public class ActivityCompleteProfile extends FragmentActivity {

    public final static String INTENT_KEY_UUID = "uuid";
    private final String LOG_TAG = "ActivityComplete";
    private final String USERNAME_UNIQUENESS_REQUIRED = "The username has been registered";



    private TextView title;
    private TextView display;
    private EditText editxtUsername;
    private EditText editxtCountry;
    private Button btnConfirm;
    private String uuid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Typeface newFont=Typeface.createFromAsset(getAssets(),"fonts/Patchwork_Stitchlings.ttf");
        uuid = getIntent().getExtras().getString(INTENT_KEY_UUID);


        title = (TextView) findViewById(R.id.activity_complete_profile_txtview_title);
        title.setTypeface(newFont);
        display = (TextView) findViewById(R.id.activity_complete_profile_txtview_result);
        editxtUsername = (EditText) findViewById(R.id.activity_complete_profile_editxt_username);
        editxtCountry = (EditText) findViewById(R.id.activity_complete_profile_editxt_country);
        editxtCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundHelper.getInstance(getApplicationContext()).playButtonClick();
                final CountryPicker picker = CountryPicker.newInstance("Select Country");
                picker.show( getSupportFragmentManager(), "COUNTRY_PICKER");
                picker.setListener(new CountryPickerListener() {
                    @Override
                    public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {
                        // Implement your code here
                        editxtCountry.setText(name);
                        picker.dismiss();
                    }
                });
            }
        });
        btnConfirm = (Button) findViewById(R.id.activity_complete_profile_btn_confirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundHelper.getInstance(getApplicationContext()).playButtonClick();
                InputHelper inputHelper = new InputHelper();
                try {
                    if( inputHelper.isValid(editxtUsername, InputHelper.INPUT_TYPE_USERNAME)
                            && inputHelper.isValid(editxtCountry, InputHelper.INPUT_TYPE_COUNTRY) ){
                        DialogHelper.startLoading(ActivityCompleteProfile.this);
                        final String username = editxtUsername.getText().toString().trim();
                        final String country = editxtCountry.getText().toString();
                        FirebaseDatabaseHelper.getInstance().isUsernameExist(username, new FirebaseDatabaseHelper.BooleanCallback() {
                            @Override
                            public void onTrue(@Nullable String str) {
                                DialogHelper.endLoading();
                                feedbackDisplay(USERNAME_UNIQUENESS_REQUIRED);
                            }

                            @Override
                            public void onFalse(@Nullable String str) {
                                User newUser = new User(uuid, username, country);
                                FirebaseDatabaseHelper.getInstance().addNewUser(newUser);
                                GlobalData.getInstance().getMe().setUsername(username);
                                GlobalData.getInstance().getMe().setCountry(country);
                                GlobalData.getInstance().getMe().setTotalGameNum(newUser.getTotalGameNum());
                                GlobalData.getInstance().getMe().setWinningGameNum(newUser.getWinningGameNum());
                                GlobalData.getInstance().getMe().setInvitable(newUser.isInvitable());
                                DialogHelper.endLoading();
                                Intent intent = new Intent(ActivityCompleteProfile.this, ActivityMain.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        feedbackDisplay(inputHelper.getLastResult());
                    }
                } catch (InputHelper.InputException e) {
                    feedbackDisplay(e.getMessage());
                }
            }
        });


    }



    private void feedbackDisplay(String msg){
        if( display==null ){
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        } else {
            display.setText(msg);
            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    display.setText("");
                }
            };
            handler.postDelayed(runnable, 4000);
        }
    }



}
