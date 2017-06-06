package com.moutaigua.ultimatetictactoe;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by mou on 3/22/17.
 */

public class GameSetting {
    public static final String SHARED_PREF_KEY_INVITATION = "ttt_invitation";
    public static final String SHARED_PREF_KEY_SOUND_EFFECT = "ttt_sound_effect";

    private boolean invitationAllowed;
    private boolean soundEffectAllowed;

    public GameSetting(){
        invitationAllowed = true;
        soundEffectAllowed = true;
    }


    public void loadSetting(Context context){
        SharedPreferences pref = context.getSharedPreferences(GlobalData.getInstance().getMe().getUuid(), Context.MODE_PRIVATE);
        boolean invitation = pref.getBoolean(SHARED_PREF_KEY_INVITATION, true);
        boolean sound_effect = pref.getBoolean(SHARED_PREF_KEY_SOUND_EFFECT, true);
        setInvitationAllowed(invitation);
        setSoundEffectAllowed(sound_effect);
    }

    public void saveSetting(Context context){
        SharedPreferences pref = context.getSharedPreferences(GlobalData.getInstance().getMe().getUuid(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(SHARED_PREF_KEY_INVITATION, invitationAllowed);
        editor.putBoolean(SHARED_PREF_KEY_SOUND_EFFECT, soundEffectAllowed);
        editor.apply();
    }

    public boolean isInvitationAllowed() {
        return invitationAllowed;
    }

    public void setInvitationAllowed(boolean invitationAllowed) {
        this.invitationAllowed = invitationAllowed;
    }

    public boolean isSoundEffectAllowed() {
        return soundEffectAllowed;
    }

    public void setSoundEffectAllowed(boolean soundEffectAllowed) {
        this.soundEffectAllowed = soundEffectAllowed;
    }
}
