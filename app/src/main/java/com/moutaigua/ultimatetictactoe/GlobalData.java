package com.moutaigua.ultimatetictactoe;

import java.text.DecimalFormat;

/**
 * Created by mou on 3/7/17.
 */

public class GlobalData {

    private static GlobalData myInstance;

    private User me;
    private GameRoom myRoom;
    private GameSetting setting;
    private boolean isLoginByEmail;

    private GlobalData(){
        me = new User();
        myRoom = new GameRoom();
        setting = new GameSetting();
    }

    public static synchronized GlobalData getInstance(){
        if(myInstance==null){
            myInstance = new GlobalData();
        }
        return myInstance;
    }



    /****** Controllers ******/

    public void updateWinningRate(boolean isWin) {
        me.setTotalGameNum( me.getTotalGameNum()+1 );
        if( isWin ){
            me.setWinningGameNum( me.getWinningGameNum()+1 );
        }
        float newRate = (float)me.getWinningGameNum() / me.getTotalGameNum();
        me.setWinningRate(newRate);
    }


    /***** Getters and Setters *****/
    public User getMe() {
        return me;
    }

    public void setMe(User me) {
        this.me = me;
    }

    public GameRoom getMyRoom() {
        return this.myRoom;
    }

    public GameSetting getSetting() {
        return setting;
    }

    public boolean isLoginByEmail() {
        return isLoginByEmail;
    }

    public void setLoginByEmail(boolean loginByEmail) {
        isLoginByEmail = loginByEmail;
    }

}
