package com.moutaigua.ultimatetictactoe;

import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mukesh.countrypicker.fragments.CountryPicker;
import com.mukesh.countrypicker.interfaces.CountryPickerListener;

public class ActivityRegister extends FragmentActivity {

    private final String LOG_TAG = "ActivityRegister";
    private final String EMAIL_UNIQUENESS_REQUIRED = "The email has been registered";
    private final String USERNAME_UNIQUENESS_REQUIRED = "The username has been registered";

    private TextView title;
    private TextView display;
    private EditText editxtUsername;
    private EditText editxtEmail;
    private EditText editxtPassword;
    private EditText editxtCountry;
    private Button btnRegister;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Typeface newFont=Typeface.createFromAsset(getAssets(),"fonts/Patchwork_Stitchlings.ttf");


        title = (TextView) findViewById(R.id.activity_register_txtview_title);
        title.setTypeface(newFont);
        display = (TextView) findViewById(R.id.activity_register_txtview_result);
        editxtUsername = (EditText) findViewById(R.id.activity_register_editxt_username);
        editxtEmail = (EditText) findViewById(R.id.activity_register_editxt_email);
        editxtPassword = (EditText) findViewById(R.id.activity_register_editxt_password);
        editxtCountry = (EditText) findViewById(R.id.activity_register_editxt_country);
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
        btnRegister = (Button) findViewById(R.id.activity_register_btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundHelper.getInstance(getApplicationContext()).playButtonClick();
                InputHelper inputHelper = new InputHelper();
                try {
                    if( inputHelper.isValid(editxtEmail, InputHelper.INPUT_TYPE_EMAIL)
                            && inputHelper.isValid(editxtPassword, InputHelper.INPUT_TYPE_PASSWORD)
                            && inputHelper.isValid(editxtUsername, InputHelper.INPUT_TYPE_USERNAME)
                            && inputHelper.isValid(editxtCountry, InputHelper.INPUT_TYPE_COUNTRY) ){
                        final String userName = editxtUsername.getText().toString().trim();
                        final String email = editxtEmail.getText().toString().trim();
                        final String password = editxtPassword.getText().toString();
                        final String country = editxtCountry.getText().toString();
                        DialogHelper.startLoading(ActivityRegister.this);
                        FirebaseDatabaseHelper.getInstance().isUsernameExist(userName, new FirebaseDatabaseHelper.BooleanCallback() {
                            @Override
                            public void onTrue(@Nullable String str) {
                                feedbackDisplay(USERNAME_UNIQUENESS_REQUIRED);
                                DialogHelper.endLoading();
                            }

                            @Override
                            public void onFalse(@Nullable String str) {
                                User newUser = new User();
                                newUser.setUsername(userName);
                                newUser.setEmail(email);
                                newUser.setCountry(country);
                                FirebaseAuthHelper.getInstance(ActivityRegister.this).register(newUser, password, new FirebaseAuthHelper.Feedback() {
                                    @Override
                                    public void onSuccess(String msg) {
                                        DialogHelper.endLoading();
                                    }

                                    @Override
                                    public void onErrorFeedback(String errorMsg) {
                                        Log.d(LOG_TAG, errorMsg);
                                        DialogHelper.endLoading();
                                        if( errorMsg.equalsIgnoreCase("The email address is already in use by another account.") ) {
                                            feedbackDisplay(EMAIL_UNIQUENESS_REQUIRED);
                                        }
                                    }
                                });
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


    @Override
    public void onBackPressed() {
        SoundHelper.getInstance(getApplicationContext()).playButtonClick();
        super.onBackPressed();
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
