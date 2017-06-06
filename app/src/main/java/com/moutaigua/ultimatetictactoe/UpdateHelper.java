package com.moutaigua.ultimatetictactoe;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

/**
 * Created by mou on 3/15/17.
 */

public class UpdateHelper {

    private static UpdateHelper myInstance;
    private Context ctxt;

    public static final String VERSION = "1.0";

    private UpdateHelper(Context context){
        ctxt = context;
    }

    public static synchronized UpdateHelper getInstance(Context context){
        if( myInstance==null ){
            myInstance = new UpdateHelper(context);
        }
        return myInstance;
    }




    public void updateIfNecessary(){
        FirebaseDatabaseHelper.getInstance().isLatestVersion(VERSION, new FirebaseDatabaseHelper.BooleanCallback() {
            @Override
            public void onTrue(@Nullable String str) {

            }

            @Override
            public void onFalse(@Nullable String str) {
                DialogHelper.openUpdateDialog(ctxt);
            }
        });
    }




    public void setLatestVersion(){
        FirebaseDatabaseHelper.getInstance().setLatestVersion(VERSION);
    }

}
