package com.moutaigua.ultimatetictactoe;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.sql.SQLOutput;

public class ActivityLogin extends Activity {

    public static final String SHARED_PREF_EMAIL_PASSWORD = "ultimate_ttt";
    public static final String SHARED_PREF_KEY_EMAIL = "ttt_email";
    public static final String SHARED_PREF_KEY_PASSWORD = "ttt_password";

    private final String LOG_TAG = "ActivityLogin";
    private final int REQUEST_REGISTER = 1;

    private TextView title;
    private TextView display;
    private EditText editxtEmail;
    private EditText editxtPassword;
    private Button btnEmailLogin;
    private TextView txtviewForgotPassword;
    private TextView txtviewSignUp;
    private CallbackManager callbackManager;
    private LoginButton btnFacebookLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Typeface newFont=Typeface.createFromAsset(getAssets(),"fonts/Patchwork_Stitchlings.ttf");

        FacebookSdk.sdkInitialize(getApplicationContext());
        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }
        AppEventsLogger.activateApp(this);

        title = (TextView) findViewById(R.id.activity_login_txtview_title);
        title.setTypeface(newFont);


        // check network
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if( !isConnected ){
            Toast.makeText(
                    this,
                    "I need Internet service. Please check your network status and open me again. Thanks!",
                    Toast.LENGTH_LONG
            );
            return;
        }
        // update to latest version
//        UpdateHelper.getInstance(this).setLatestVersion();
        UpdateHelper.getInstance(this).updateIfNecessary();



        // Email Login
        SharedPreferences pref = getSharedPreferences(SHARED_PREF_EMAIL_PASSWORD, Context.MODE_PRIVATE);
        String rememberedEmail = pref.getString(SHARED_PREF_KEY_EMAIL, null);
        if( rememberedEmail!=null ){
            String rememberedPassword = pref.getString(SHARED_PREF_KEY_PASSWORD, null);
            Log.d(LOG_TAG, rememberedEmail+"+"+rememberedPassword);
            DialogHelper.startLoading(this);
            FirebaseAuthHelper.getInstance(this).loginByEmailAndPassword(
                    rememberedEmail,
                    rememberedPassword,
                    new FirebaseAuthHelper.Feedback() {
                        @Override
                        public void onSuccess(String meg) {
                            DialogHelper.endLoading();
                        }

                        @Override
                        public void onErrorFeedback(String errorMsg) {
                            DialogHelper.endLoading();
                            feedbackDisplay(errorMsg);
                        }
                    });
        }
        display = (TextView) findViewById(R.id.activity_login_txtview_result);
        editxtEmail = (EditText) findViewById(R.id.activity_login_editxt_email);
        editxtPassword = (EditText) findViewById(R.id.activity_login_editxt_password);
        btnEmailLogin = (Button) findViewById(R.id.activity_login_btn_sign_in);
        btnEmailLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundHelper.getInstance(getApplicationContext()).playButtonClick();
                InputHelper inputHelper = new InputHelper();
                try {
                    if( inputHelper.isValid(editxtEmail, InputHelper.INPUT_TYPE_EMAIL)
                            && inputHelper.isValid(editxtPassword, InputHelper.INPUT_TYPE_PASSWORD) ){
                        Log.d(LOG_TAG, "Input Correct");
                        final String email = editxtEmail.getText().toString().trim();
                        final String password = editxtPassword.getText().toString();
                        DialogHelper.startLoading(ActivityLogin.this);
                        FirebaseAuthHelper.getInstance(ActivityLogin.this).loginByEmailAndPassword(
                                email,
                                password,
                                new FirebaseAuthHelper.Feedback() {
                                    @Override
                                    public void onSuccess(String meg) {
                                        DialogHelper.endLoading();
                                    }

                                    @Override
                                    public void onErrorFeedback(String errorMsg) {
                                        DialogHelper.endLoading();
                                        feedbackDisplay(errorMsg);
                                    }
                                }
                        );
                    } else {
                        feedbackDisplay(inputHelper.getLastResult());
                    }
                } catch (InputHelper.InputException e) {
                    feedbackDisplay(e.getMessage());
                }
            }
        });
        txtviewForgotPassword = (TextView) findViewById(R.id.activity_login_txtview_forget_password);
        txtviewForgotPassword.setPaintFlags(txtviewForgotPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtviewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputHelper inputHelper = new InputHelper();
                try {
                    if( inputHelper.isValid(editxtEmail, InputHelper.INPUT_TYPE_EMAIL) ){
                        Log.d(LOG_TAG, "Input Correct");
                        String email = editxtEmail.getText().toString().trim();
                        FirebaseAuthHelper.getInstance(ActivityLogin.this).resetPassword(email, new FirebaseAuthHelper.Feedback() {
                            @Override
                            public void onSuccess(String msg) {
                                feedbackDisplay(msg);
                            }

                            @Override
                            public void onErrorFeedback(String errorMsg) {

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
        txtviewSignUp = (TextView) findViewById(R.id.activity_login_txtview_sign_up);
        txtviewSignUp.setPaintFlags(txtviewSignUp.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtviewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityLogin.this, ActivityRegister.class);
                startActivityForResult(intent, REQUEST_REGISTER);
            }
        });



        // Facebook Login
        Profile profile = Profile.getCurrentProfile();
        if( profile!= null ) {
            Log.d(LOG_TAG, "Facebook Logged in Already");
            DialogHelper.startLoading(ActivityLogin.this);
            FirebaseAuthHelper.getInstance(this).loginByFacebookAccessToken(
                    AccessToken.getCurrentAccessToken(),
                    new FirebaseAuthHelper.Feedback() {
                        @Override
                        public void onSuccess(String msg) {
                            DialogHelper.endLoading();
                        }

                        @Override
                        public void onErrorFeedback(String errorMsg) {
                            feedbackDisplay(errorMsg);
                        }
                    });
        }
        callbackManager = CallbackManager.Factory.create();
        FacebookCallback callback = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                DialogHelper.startLoading(ActivityLogin.this);
                Log.d(LOG_TAG, "Facebook Login Successfully");
                AccessToken accessToken = loginResult.getAccessToken();
                FirebaseAuthHelper.getInstance(ActivityLogin.this).loginByFacebookAccessToken(
                        accessToken,
                        new FirebaseAuthHelper.Feedback() {
                            @Override
                            public void onSuccess(String msg) {
                                DialogHelper.endLoading();
                            }

                            @Override
                            public void onErrorFeedback(String errorMsg) {
                                feedbackDisplay(errorMsg);
                            }
                        });
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        };
        btnFacebookLogin = (LoginButton) findViewById(R.id.activity_login_btn_facebook);
        btnFacebookLogin.setReadPermissions("public_profile","email");
        btnFacebookLogin.registerCallback(callbackManager,callback);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if( requestCode==REQUEST_REGISTER && resultCode==Activity.RESULT_OK ){
            finish();
        }
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
