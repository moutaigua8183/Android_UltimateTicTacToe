package com.moutaigua.ultimatetictactoe;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by mou on 3/10/17.
 */

public class DialogHelper {

    private static final String LOG_TAG = "DialogHelper";

    private static ProgressDialog mDialog;
    private static Context ctxt;



    /**** Update ****/

    public static void openUpdateDialog(Context context){
        ctxt = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctxt, R.style.Theme_AppCompat_Light_Dialog);
        builder.setMessage(ctxt.getString(R.string.update_required))
                .setCancelable(false)
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        goToGooglePlayPage();
                        ((Activity)ctxt).finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private static void goToGooglePlayPage(){
        final String appPackageName = ctxt.getPackageName();
        try {
            ctxt.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            ctxt.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }


    /**** Loading ****/

    public static void startLoading(Context context){
        ctxt = context;
        mDialog = new ProgressDialog(ctxt);
        try {
            mDialog.show();
        } catch (WindowManager.BadTokenException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDialog.setContentView(R.layout.dialog_loading);
    }

    public static void endLoading(){
        mDialog.dismiss();
        mDialog = null;
    }


    /**** Exit Alert ****/

    public static void openDialogForMultiplerExit(Context context){
        ctxt = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctxt, R.style.Theme_AppCompat_Light_Dialog);
        builder.setMessage(ctxt.getString(R.string.game_message_normal_exit))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SoundHelper.getInstance(ctxt).playButtonClick();
                        dialog.dismiss();
                        ((Activity)ctxt).finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        SoundHelper.getInstance(ctxt).playButtonClick();
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void openExitConfirmInOnlineRoom(Context context){
        ctxt = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctxt, R.style.Theme_AppCompat_Light_Dialog);
        builder.setMessage(ctxt.getString(R.string.game_message_normal_exit))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SoundHelper.getInstance(ctxt).playButtonClick();
                        FirebaseDatabaseHelper.getInstance().leaveRoom();
                        dialog.dismiss();
                        ((Activity)ctxt).finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        SoundHelper.getInstance(ctxt).playButtonClick();
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void openExitAlertInOnlineRoom(Context context){
        ctxt = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctxt, R.style.Theme_AppCompat_Light_Dialog);
        builder.setMessage(ctxt.getString(R.string.game_message_abnormal_exit))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SoundHelper.getInstance(ctxt).playButtonClick();
                        FirebaseDatabaseHelper.getInstance().leaveRoom();
                        dialog.dismiss();
                        ((Activity)ctxt).finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        SoundHelper.getInstance(ctxt).playButtonClick();
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void openDialogAfterOpponentExit(Context context, final FirebaseDatabaseHelper.GameRoomCallback callback){
        ctxt = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctxt, R.style.Theme_AppCompat_Light_Dialog);
        builder.setMessage(ctxt.getString(R.string.game_message_opponent_exit))
                .setCancelable(false)
                .setPositiveButton("Match", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SoundHelper.getInstance(ctxt).playButtonClick();
                        FirebaseDatabaseHelper.getInstance().stopGameRoomListeners();
                        FirebaseFunctionHelper.getInstance(ctxt).stopServerInactiveCheck(GlobalData.getInstance().getMyRoom().getRoomId());
                        FirebaseDatabaseHelper.getInstance().deteleRoom( GlobalData.getInstance().getMyRoom().getRoomId() );
                        GlobalData.getInstance().getMyRoom().reset();
                        dialog.dismiss();
                        startMatching(ctxt, callback);
                    }
                })
                .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        SoundHelper.getInstance(ctxt).playButtonClick();
                        FirebaseFunctionHelper.getInstance(ctxt).stopServerInactiveCheck(GlobalData.getInstance().getMyRoom().getRoomId());
                        FirebaseDatabaseHelper.getInstance().deteleRoom( GlobalData.getInstance().getMyRoom().getRoomId() );
                        dialog.dismiss();
                        ((Activity)ctxt).finish();
                        ctxt = null;
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    /**** Inactive Situation ****/

    public static void openDialogForMyInactiveness(Context context, final FirebaseDatabaseHelper.GameRoomCallback callback){
        ctxt = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctxt, R.style.Theme_AppCompat_Light_Dialog);
        builder.setMessage(ctxt.getString(R.string.game_message_me_inactive))
                .setCancelable(false)
                .setPositiveButton("Match", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SoundHelper.getInstance(ctxt).playButtonClick();
                        FirebaseDatabaseHelper.getInstance().setOnline(GlobalData.getInstance().getMe());
                        FirebaseDatabaseHelper.getInstance().stopGameRoomListeners();
                        GlobalData.getInstance().getMyRoom().reset();
                        dialog.dismiss();
                        startMatching(ctxt, callback);
                    }
                })
                .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        SoundHelper.getInstance(ctxt).playButtonClick();
                        FirebaseDatabaseHelper.getInstance().setOnline(GlobalData.getInstance().getMe());
                        dialog.dismiss();
                        ((Activity)ctxt).finish();
                        ctxt = null;
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void openDialogForInactiveOpponent(Context context, final FirebaseDatabaseHelper.GameRoomCallback callback){
        ctxt = context;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctxt, R.style.Theme_AppCompat_Light_Dialog);
        builder.setMessage(ctxt.getString(R.string.game_message_opponent_inactive))
                .setCancelable(false)
                .setPositiveButton("Match", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SoundHelper.getInstance(ctxt).playButtonClick();
                        FirebaseDatabaseHelper.getInstance().stopGameRoomListeners();
                        GlobalData.getInstance().getMyRoom().reset();
                        dialog.dismiss();
                        startMatching(ctxt, callback);
                    }
                })
                .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        SoundHelper.getInstance(ctxt).playButtonClick();
                        dialog.dismiss();
                        ((Activity)ctxt).finish();
                        ctxt = null;
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    /**** Match ****/

    public static void startMatching(final Context context, final FirebaseDatabaseHelper.GameRoomCallback callback) {
        ctxt = context;
        mDialog = new ProgressDialog(context);
        try {
            mDialog.show();
        } catch (WindowManager.BadTokenException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDialog.setContentView(R.layout.dialog_matching);
        Button cancelBtn = (Button) mDialog.findViewById(R.id.dialog_matching_btn_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundHelper.getInstance(ctxt).playButtonClick();
                endMatching(false);
            }
        });
        Typeface newFont = Typeface.createFromAsset(ctxt.getAssets(),"fonts/Sansation_Light.ttf");
        TextView matchingHint = (TextView) mDialog.findViewById(R.id.dialog_matching_txtview_matching);
        matchingHint.setTypeface(newFont);
        TextView display = (TextView) mDialog.findViewById(R.id.dialog_matching_txtview_online_number);
        display.setTypeface(newFont);
        FirebaseDatabaseHelper.getInstance().startListenOnlineUsers(display);
        FirebaseDatabaseHelper.getInstance().getWaitingRoom(new FirebaseDatabaseHelper.BooleanCallback() {
            @Override
            public void onTrue(@Nullable String roomId) {
                // join an existing room
                Log.d(LOG_TAG, "Join the room: " + roomId);
                GlobalData.getInstance().getMyRoom().setRoomId(roomId);
                GlobalData.getInstance().getMyRoom().setWhoAmI(FragmentBoard.PLAYER_2);
                FirebaseDatabaseHelper.getInstance().registerMyInfo(roomId);
                FirebaseDatabaseHelper.getInstance().closeRoom();
                // start listeners
                FirebaseDatabaseHelper.getInstance().startGameRoomListeners(callback);
            }

            @Override
            public void onFalse(@Nullable String str) {
                // create a new room
                FirebaseDatabaseHelper.getInstance().createNewRoom(new FirebaseDatabaseHelper.Callback() {
                    @Override
                    public void onPostExcute(@Nullable String roomId) {
                        Log.d(LOG_TAG, "Create a room: " + roomId);
                        GlobalData.getInstance().getMyRoom().setRoomId(roomId);
                        GlobalData.getInstance().getMyRoom().setWhoAmI(FragmentBoard.PLAYER_1);
                        // start listeners
                        FirebaseDatabaseHelper.getInstance().startGameRoomListeners(callback);
                        FirebaseFunctionHelper.getInstance(context).requestServerInactiveCheck(roomId);
                    }
                });
            }
        });
    }

    public static void endMatching(boolean isMatched){
        mDialog.dismiss();
        FirebaseDatabaseHelper.getInstance().stopListenOnlineUsers();
        if( !isMatched ){
            FirebaseFunctionHelper.getInstance(ctxt).stopServerInactiveCheck(GlobalData.getInstance().getMyRoom().getRoomId());
            FirebaseDatabaseHelper.getInstance().deteleRoom( GlobalData.getInstance().getMyRoom().getRoomId() );
            ((Activity)ctxt).finish();
            ctxt = null;
        }
    }

}
