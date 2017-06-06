package com.moutaigua.ultimatetictactoe;

import android.graphics.Bitmap;

import java.text.DecimalFormat;

/**
 * Created by mou on 3/7/17.
 */

public class User {

    private String uuid;
    private String username;
    private String email;
    private String country;
    private int totalGameNum;
    private int winningGameNum;
    private float winningRate;
    private boolean invitable;



    public User(){
        this.uuid = "";
        this.username = "";
        this.email = "";
        this.country = "";
        this.totalGameNum = 0;
        this.winningGameNum = 0;
        this.winningRate = 0;
        this.invitable = true;
    }

    public User(String uuid, String username, String country) {
        this.uuid = uuid;
        this.username = username;
        this.country = country;
        this.totalGameNum = 0;
        this.winningGameNum = 0;
        this.winningRate = 0;
        this.invitable = true;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String email) {
        this.uuid = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getTotalGameNum() {
        return totalGameNum;
    }

    public void setTotalGameNum(int totalGameNum) {
        this.totalGameNum = totalGameNum;
    }

    public int getWinningGameNum() {
        return winningGameNum;
    }

    public void setWinningGameNum(int winningGameNum) {
        this.winningGameNum = winningGameNum;
    }

    public String getWinningPercent(){
        return String.format(java.util.Locale.US, "%.2f", winningRate*100) + "%";
    }

    public float getWinningRate() {
        return winningRate;
    }

    public void setWinningRate(float winningRate) {
        this.winningRate = winningRate;
    }

    public boolean isInvitable() {
        return invitable;
    }

    public void setInvitable(boolean invitable) {
        this.invitable = invitable;
    }

}
