package com.moutaigua.ultimatetictactoe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

/**
 * Created by mou on 3/6/17.
 */

public class FirebaseAuthHelper {


    private final String LOG_TAG = "FirebaseAuthHelper";




    private static FirebaseAuthHelper myInstance;
    private static Context ctxt;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;



    private FirebaseAuthHelper(Context context){
        ctxt = context;
        mAuth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseAuthHelper getInstance(Context context){
        if( myInstance==null ){
            myInstance = new FirebaseAuthHelper(context);
        } else {
            ctxt = context;
        }
        return myInstance;
    }



    public interface Feedback {
        void onSuccess(String msg);
        void onErrorFeedback(String errorMsg);
    }






    /***** Email Login *****/

    public void loginByEmailAndPassword(final String email, final String password, final Feedback feedback) {
        mAuth.removeAuthStateListener(mAuthListener);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if( user!=null ){

                } else {

                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOG_TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(LOG_TAG, "signInWithEmail:failed", task.getException());
                            String msg = null;
                            if( task.getException().getMessage().equalsIgnoreCase("The password is invalid or the user does not have a password.") ){
                                msg = "Password is incorrect or try using Facebook Login";
                            } else if ( task.getException().getMessage().equalsIgnoreCase("There is no user record corresponding to this identifier. The user may have been deleted.") ){
                                msg = "For new users, Please sign up first";
                            } else {
                                msg = "Unknow error happens";
                            }
                            feedback.onErrorFeedback(msg);
                        } else {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            if(user!=null) {
                                Log.d(LOG_TAG, "onAuthStateChanged:signed_in: " + user.getEmail());
                                rememberEmailAndPassword(email, password);
                                GlobalData.getInstance().setLoginByEmail(true);
                                GlobalData.getInstance().getMe().setEmail(user.getEmail());
                                completeOrFetchProfile(user.getUid());
                            }
                        }
                    }
                });
    }


    /***** Facebook Login *****/

    public void loginByFacebookAccessToken(final AccessToken token, final Feedback feedback) {
        mAuth.removeAuthStateListener(mAuthListener);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user==null) {
                    LoginManager.getInstance().logOut();
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOG_TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w(LOG_TAG, task.getException());
                            feedback.onErrorFeedback("Try using email and password to log in");
                        } else {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            Log.d(LOG_TAG, "onAuthStateChanged:signed_in: " + user.getEmail());
                            GlobalData.getInstance().setLoginByEmail(false);
                            GlobalData.getInstance().getMe().setEmail(user.getEmail());
                            completeOrFetchProfile(user.getUid());
                        }
                    }
                });
    }


    /***** Register or Reset *****/

    public void register(final User newUser, final String password, final Feedback feedback){
        mAuth.removeAuthStateListener(mAuthListener);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };
        mAuth.addAuthStateListener(mAuthListener);
        mAuth.createUserWithEmailAndPassword(newUser.getEmail(), password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser != null) {
                            Log.d(LOG_TAG, "onAuthStateChanged:signed_in:" + firebaseUser.getEmail());
                            GlobalData.getInstance().setLoginByEmail(true);
                            newUser.setUuid( firebaseUser.getUid() );
                            GlobalData.getInstance().getMe().setUuid(newUser.getUuid());
                            GlobalData.getInstance().getMe().setUsername(newUser.getUsername());
                            GlobalData.getInstance().getMe().setEmail(newUser.getEmail());
                            GlobalData.getInstance().getMe().setCountry(newUser.getCountry());
                            GlobalData.getInstance().getMe().setTotalGameNum(newUser.getTotalGameNum());
                            GlobalData.getInstance().getMe().setWinningGameNum(newUser.getWinningGameNum());
                            GlobalData.getInstance().getMe().setWinningRate(newUser.getWinningRate());
                            GlobalData.getInstance().getMe().setInvitable(newUser.isInvitable());
                            FirebaseDatabaseHelper.getInstance().addNewUser(newUser);
                            Intent intent = new Intent(ctxt, ActivityMain.class);
                            ctxt.startActivity(intent);
                            ((Activity)ctxt).setResult(Activity.RESULT_OK);
                            ((Activity)ctxt).finish();
                        } else {
                            feedback.onErrorFeedback(task.getException().getMessage());
                        }
                    }
                });
    }

    public void resetPassword(String email, final Feedback feedback){
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            feedback.onSuccess("Email sent");
                        } else {
                            Log.w(LOG_TAG, task.getException().getMessage());
                        }
                    }
                });
    }


    /***** After Login *****/

    private void rememberEmailAndPassword(String email, String password){
        SharedPreferences pref = ctxt.getSharedPreferences(ActivityLogin.SHARED_PREF_EMAIL_PASSWORD, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(ActivityLogin.SHARED_PREF_KEY_EMAIL, email);
        editor.putString(ActivityLogin.SHARED_PREF_KEY_PASSWORD, password);
        editor.commit();
    }

    private void completeOrFetchProfile(final String uuid){
        FirebaseDatabaseHelper.getInstance().isUuidExist(uuid, new FirebaseDatabaseHelper.BooleanCallback() {
            @Override
            public void onTrue(@Nullable String str) {
                FirebaseDatabaseHelper.getInstance().getUserProfile(uuid, new FirebaseDatabaseHelper.UserCallback() {
                    @Override
                    public void onComplete(ArrayList<User> userList) {
                        GlobalData.getInstance().setMe( userList.get(0) );
                        Intent intent = new Intent(ctxt, ActivityMain.class);
                        ctxt.startActivity(intent);
                        ((Activity)ctxt).finish();
                    }
                });
            }

            @Override
            public void onFalse(@Nullable String str) {
                Intent intent = new Intent(ctxt, ActivityCompleteProfile.class);
                intent.putExtra(ActivityCompleteProfile.INTENT_KEY_UUID, uuid);
                ctxt.startActivity(intent);
                ((Activity)ctxt).finish();
            }
        });
    }

    public void signOut(){
        mAuth.signOut();
    }





}
