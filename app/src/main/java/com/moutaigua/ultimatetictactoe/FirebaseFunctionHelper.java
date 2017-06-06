package com.moutaigua.ultimatetictactoe;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by mou on 3/6/17.
 */

public class FirebaseFunctionHelper {


    private final String LOG_TAG = "FirebaseFunctionHelper";




    private static FirebaseFunctionHelper myInstance;
    private static Context ctxt;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;



    private FirebaseFunctionHelper(Context context){
        ctxt = context;
        mAuth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseFunctionHelper getInstance(Context context){
        if( myInstance==null ){
            myInstance = new FirebaseFunctionHelper(context);
        } else {
            ctxt = context;
        }
        return myInstance;
    }



   public void requestServerInactiveCheck(String roomId){
       String url ="https://us-central1-ultimate-tic-tac-toe-49757.cloudfunctions.net/listenRoom";
       url = url + "?id=" + roomId;
       JsonObjectRequest request = new JsonObjectRequest(
               Request.Method.GET,  url,  null,
               new Response.Listener<JSONObject>() {
                   @Override
                   public void onResponse(JSONObject response) {
                       try {
                           Log.d(LOG_TAG, "Request server " + response.getString("res"));
                       } catch (JSONException e) {
                           e.printStackTrace();
                       }
                   }
               },
               new Response.ErrorListener() {
                   @Override
                   public void onErrorResponse(VolleyError error) {
                       Log.e(LOG_TAG, error.getMessage().toString());
                   }
               }
       );
       VolleyHelper.getInstance(ctxt).addToRequestQueue(request);
   }

    public void stopServerInactiveCheck(String roomId){
        String url ="https://us-central1-ultimate-tic-tac-toe-49757.cloudfunctions.net/stopListenRoom";
        url = url + "?id=" + roomId;
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,  url,  null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(LOG_TAG, "Stop server " + response.getString("res"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(LOG_TAG, error.getMessage().toString());
                    }
                }
        );
        VolleyHelper.getInstance(ctxt).addToRequestQueue(request);
    }





}
