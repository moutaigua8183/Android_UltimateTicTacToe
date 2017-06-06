package com.moutaigua.ultimatetictactoe;

import android.app.Application;
import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by mou on 3/22/17.
 */

public class SoundHelper extends Application {

    private static SoundHelper myInstance;
    private Context ctxt;
    private MediaPlayer mediaPlayer;


    private SoundHelper(Context context){
        this.ctxt = context;
        mediaPlayer = null;
    }

    public static synchronized SoundHelper getInstance(Context context){
        if( myInstance==null ){
            myInstance = new SoundHelper(context);
        }
        return myInstance;
    }

    public void playButtonClick(){
        if( mediaPlayer!=null ){
            mediaPlayer.release();
        }
        if( GlobalData.getInstance().getSetting().isSoundEffectAllowed() ) {
            mediaPlayer = MediaPlayer.create(ctxt, R.raw.sound_button_click);
            mediaPlayer.start();
        }
    }

    public void playToggleClickOn(){
        if( mediaPlayer!=null ){
            mediaPlayer.release();
        }
        if( GlobalData.getInstance().getSetting().isSoundEffectAllowed() ) {
            mediaPlayer = MediaPlayer.create(ctxt, R.raw.sound_toggle_click_on);
            mediaPlayer.start();
        }
    }

    public void playToggleClickOff(){
        if( mediaPlayer!=null ){
            mediaPlayer.release();
        }
        if( GlobalData.getInstance().getSetting().isSoundEffectAllowed() ) {
            mediaPlayer = MediaPlayer.create(ctxt, R.raw.sound_toggle_click_off);
            mediaPlayer.start();
        }
    }

    public void playGameHint(){
        if( mediaPlayer!=null ){
            mediaPlayer.release();
        }
        if( GlobalData.getInstance().getSetting().isSoundEffectAllowed() ) {
            mediaPlayer = MediaPlayer.create(ctxt, R.raw.sound_hint);
            mediaPlayer.start();
        }
    }

    public void playGameStart(){
        if( mediaPlayer!=null ){
            mediaPlayer.release();
        }
        if( GlobalData.getInstance().getSetting().isSoundEffectAllowed() ) {
            mediaPlayer = MediaPlayer.create(ctxt, R.raw.sound_game_start);
            mediaPlayer.start();
        }
    }

    public void playGameOver(){
        if( mediaPlayer!=null ){
            mediaPlayer.release();
        }
        if( GlobalData.getInstance().getSetting().isSoundEffectAllowed() ) {
            mediaPlayer = MediaPlayer.create(ctxt, R.raw.sound_game_over);
            mediaPlayer.start();
        }
    }

    public void playMoveValid(){
        if( mediaPlayer!=null ){
            mediaPlayer.release();
        }
        if( GlobalData.getInstance().getSetting().isSoundEffectAllowed() ) {
            mediaPlayer = MediaPlayer.create(ctxt, R.raw.sound_move_valid);
            mediaPlayer.start();
        }
    }

}
