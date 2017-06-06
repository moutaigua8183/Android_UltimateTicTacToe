package com.moutaigua.ultimatetictactoe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by mou on 3/10/17.
 * Used to listen to player's status, such as online, away and offline
 * The status is useful during an online game and for an online game invitation.
 */

public class UserStatusSyncService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        
    }
}
